# 构建镜像，执行命令：【docker build -t renren_io:2.0 .】
FROM eclipse-temurin:17-jre
MAINTAINER Mark
# 时区问题
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo 'Asia/Shanghai' >/etc/timezone

# 字体
COPY fonts/simsun.ttc /usr/share/fonts/simsun.ttc


ENTRYPOINT ["java", "-server", "-Xms512M", "-Xmx512M", "-Djava.security.egd=file:/dev/./urandom", "-Dfile.encoding=UTF-8", "-XX:+HeapDumpOnOutOfMemoryError", "-jar", "/app/app.jar" ]


