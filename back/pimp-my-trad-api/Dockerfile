FROM openjdk:8-jre
RUN apt-get update && apt-get install -y git && \
	git config --global user.email "pimpmytrad@oui.sncf" && \
	git config --global user.name "Pimp my Trad" && \
	mkdir -p /usr/src/pmt && \
	mkdir -p /opt/pmtdata && \
	chown -R www-data:www-data /opt/pmtdata && \
	chmod 755 -R /opt/pmtdata
WORKDIR /usr/src/pmt
COPY ./target/pimp-my-trad-api-*.jar /usr/src/pmt/pmt.jar
CMD sh -c "java -jar pmt.jar --spring.profiles.active=${SPRING_PROFILE}"