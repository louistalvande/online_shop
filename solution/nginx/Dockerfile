# Stage: build buyer-portal
FROM node:20 AS buyer-build
WORKDIR /app
COPY frontend/buyer-portal/package*.json ./
RUN npm ci
COPY frontend/buyer-portal/ ./
RUN npm run build

# Stage: build vendor-backoffice
FROM node:20 AS vendor-build
WORKDIR /app
COPY frontend/vendor-backoffice/package*.json ./
RUN npm ci
COPY frontend/vendor-backoffice/ ./
RUN npm run build

# Stage: build admin-console
FROM node:20 AS admin-build
WORKDIR /app
COPY frontend/admin-console/package*.json ./
RUN npm ci
COPY frontend/admin-console/ ./
RUN npm run build

# Final stage: Nginx serves all three apps
FROM nginx:alpine
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY --from=buyer-build /app/dist  /usr/share/nginx/html/buyer
COPY --from=vendor-build /app/dist /usr/share/nginx/html/vendor
COPY --from=admin-build /app/dist  /usr/share/nginx/html/admin
EXPOSE 80
