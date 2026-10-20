FROM tomcat:10.1-jdk17-temurin-noble@sha256:a04d06813aa9004cb6b40ca4519d70d06f3b91f6c028f10ae285ea3e832344fb

RUN apt-get update \
    && apt-get install -y --no-install-recommends git python3 postgresql-client sudo \
    && rm -rf /var/lib/apt/lists/* \
    && if id -u ubuntu >/dev/null 2>&1; then \
         usermod --login vscode --home /home/vscode --move-home --shell /bin/bash ubuntu; \
         groupmod --new-name vscode ubuntu; \
       else \
         useradd --uid 1000 --create-home --shell /bin/bash vscode; \
       fi \
    && usermod -aG sudo vscode \
    && echo 'vscode ALL=(ALL) NOPASSWD:ALL' > /etc/sudoers.d/vscode

COPY lab /usr/local/bin/lab
RUN chmod 0755 /usr/local/bin/lab

ENV LAB_CONTAINER=1 \
    LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    PGHOST=127.0.0.1 \
    PGPORT=5432 \
    PGUSER=postgres \
    PGPASSWORD=postgres \
    PGCLIENTENCODING=UTF8

CMD ["sleep", "infinity"]
