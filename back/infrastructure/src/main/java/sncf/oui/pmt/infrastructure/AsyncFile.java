/*
 *
 *  * Copyright (C) 2018 VSCT
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package sncf.oui.pmt.infrastructure;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AsyncFile {

    private AsynchronousFileChannel channel;

    public AsyncFile(Path file, OpenOption... openOptions) throws IOException {
        channel = AsynchronousFileChannel.open(file, openOptions);
    }

    public Mono<String> readAsString() {
        return readAsString(Charset.forName("UTF-8"));
    }

    public Mono<String> readAsString(Charset charset) {
        return read().map(buf -> bufferToString(buf, charset))
                .reduce(new StringBuilder(), (acc, curr) -> {
                    acc.append(curr);
                    return acc;
                })
                .map(StringBuilder::toString);
    }

    public Flux<String> readLines() {
        return readLines(Charset.forName("UTF-8"));
    }

    public Flux<String> readLines(Charset charset) {
        return read().map(buf -> bufferToString(buf, charset))
                .flatMap(str -> Flux.fromArray(str.split("(?<=\\R)")))
                .bufferUntil(str -> Pattern.compile("\\R").matcher(str).find())
                .map(listStr -> listStr.stream()
                        .map(s -> s.replaceAll("\\R", ""))
                        .collect(Collectors.joining("")));
    }

    public Flux<ByteBuffer> read() {
        return read(128);
    }

    public Flux<ByteBuffer> read(int capacity) {
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        return Flux.create(sink -> new FileChannelCompletionHandler(channel, buffer, sink, new FileOperation() {

            @Override
            public void doOperation(AsynchronousFileChannel afc, ByteBuffer dst, long position, ByteBuffer attachment, CompletionHandler<Integer, ByteBuffer> handler) {
                afc.read(dst, position, attachment, handler);
            }

            @Override
            public Integer mutateBuffer(Integer previous, Integer pos, ByteBuffer buffer) {
                ((Buffer) buffer).clear();
                ((Buffer) buffer).limit(pos);
                return previous + pos;
            }
        }));
    }

    public Flux<ByteBuffer> write(ByteBuffer buffer) {
        if (((Buffer) buffer).position() != 0) {
            ((Buffer) buffer).flip();
        }
        return Flux.create(sink -> new FileChannelCompletionHandler(channel, buffer, sink, AsynchronousFileChannel::write));
    }

    public Flux<ByteBuffer> writeString(String str) {
        return writeString(str, Charset.forName("UTF-8"));
    }

    public Flux<ByteBuffer> writeString(String str, Charset charset) {
        ByteBuffer buffer = ByteBuffer.wrap(str.getBytes(charset));
        return write(buffer);
    }

    public void close() {
        try {
            channel.close();
        } catch (IOException ignored) {}
    }

    private static String bufferToString(ByteBuffer buf, Charset charset) {
        return new String(Arrays.copyOfRange(buf.array(), 0, buf.remaining()), charset);
    }
}

class FileChannelCompletionHandler implements CompletionHandler<Integer, ByteBuffer> {

    private FluxSink<ByteBuffer> fluxSink;
    private AsynchronousFileChannel fileChannel;
    private int counter;
    private FileOperation fileOperation;

    FileChannelCompletionHandler(AsynchronousFileChannel asynchronousFileChannel, ByteBuffer buffer, FluxSink<ByteBuffer> sink, FileOperation operation)  {
        fluxSink = sink;
        counter = 0;
        fileChannel = asynchronousFileChannel;
        fileOperation = operation;
        fileOperation.doOperation(fileChannel, buffer, counter, buffer, this);
    }

    public void completed(Integer result, ByteBuffer attachment) {
        if (result > 0) {
            counter = fileOperation.mutateBuffer(counter, result, attachment);
            fluxSink.next(attachment);
            fileOperation.doOperation(fileChannel, attachment, counter, attachment, this);
        } else {
            fluxSink.complete();
        }
    }

    public void failed(Throwable e, ByteBuffer attachment) {
        fluxSink.error(e);
    }
}

interface FileOperation {
    void doOperation(AsynchronousFileChannel afc, ByteBuffer dst, long position, ByteBuffer attachment, CompletionHandler<Integer, ByteBuffer> handler);
    default Integer mutateBuffer(Integer previous, Integer pos, ByteBuffer buffer) {
        return previous + pos;
    }
}