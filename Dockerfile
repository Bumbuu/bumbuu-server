FROM pierrevincent/gradle-java8
ADD . /app
WORKDIR /app
RUN ["gradle", "build"]
ENTRYPOINT ["gradle"]
CMD ["run"]
