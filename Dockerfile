FROM openjdk:8

WORKDIR /var/code

COPY ./build.gradle /var/code
COPY ./settings.gradle /var/code
COPY ./backend /var/code/backend
COPY ./frontend /var/code/frontend

RUN apt-get update \
    && apt-get install zip -y \
    && curl -s https://get.sdkman.io | bash

RUN /bin/bash -c "\
    source /root/.sdkman/bin/sdkman-init.sh \
        && sdk install gradle 4.4 \
        && sdk install kotlin 1.2.10"

ENV PATH="/root/.sdkman/candidates/kodtlin/current/bin:${PATH}"
ENV PATH="/root/.sdkman/candidates/gradle/current/bin:${PATH}"

RUN gradle frontend:build
RUN cp frontend/build/bundle/frontend.bundle.js backend/resources/frontend.bundle.js
RUN gradle backend:build

CMD gradle backend:run
