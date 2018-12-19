FROM openjdk:8-jre
RUN apt-get update && apt-get install -y git && \
	git config --global user.email "pimpmytrad@oui.sncf" && \
	git config --global user.name "Pimp my Trad" && \
	mkdir -p /usr/src/pmt && \
	mkdir -p /opt/pmtdata && \
	chown -R www-data:www-data /opt/pmtdata && \
	chmod 755 -R /opt/pmtdata
COPY ./target/bdd-*.jar /usr/src/pmt/test.jar
WORKDIR /usr/src/pmt
CMD ["java", "-jar", "test.jar"]