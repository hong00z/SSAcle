FROM node:22.13.1

RUN apt-get update && apt-get install -y python3-pip

WORKDIR /app

COPY .env

COPY package*.json
RUN npm install

COPY . .

EXPOSE 4001

CMD ["npm", "start"]