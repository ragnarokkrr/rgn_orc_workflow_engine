FROM mongo:4.0
COPY mongo-init/mongo-init.sh /docker-entrypoint-initdb.d/
RUN chmod +x /docker-entrypoint-initdb.d/mongo-init.sh