FROM httpd:2.4
COPY ./httpd.conf /usr/local/apache2/conf
COPY ./build/ /var/www/pmt
# COPY ./env.js /var/www/pmt/env.js
RUN apt-get update -qq && \
  	apt-get -y install gettext-base
RUN chown -R www-data:www-data /var/www/pmt
RUN chmod 755 -R /var/www/pmt
# CMD envsubst < '/var/www/pmt/env.js' > '/var/www/pmt/env.js' && httpd-foreground
CMD httpd-foreground