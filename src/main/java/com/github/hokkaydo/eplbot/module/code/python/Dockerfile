FROM python:3.9-slim
WORKDIR /usr/src/app
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt
COPY src/main/java/com/github/hokkaydo/eplbot/module/code/python/run_python.sh /usr/src/app
RUN chmod +x run_python.sh
ENTRYPOINT ["bash", "./run_python.sh"]