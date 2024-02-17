FROM node:12.18.4-alpine

LABEL maintainer=js.wildcards@gmail.com

WORKDIR /app

COPY ./package*.json ./
RUN npm ci

COPY ./ ./

CMD ["node", "."]
