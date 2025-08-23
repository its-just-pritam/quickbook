
# Quickbook

A collaborative webapp inspired from Google Docs, tailored for code sharing.
- No Login or Registration required
- Open a session and start coding
- Quick share document link with you team
- Build your project together in real-time

## Backend API Reference

#### Capture Document Activity

```http
POST /ops/{docId}/user/{userId}
```

```bash
[
    {
        "opType": "insert",
        "position": 0,
        "content": "Hi"
    },
    {
        "opType": "insert",
        "position": 1,
        "content": "there!"
    },
    {
        "opType": "delete",
        "position": 1,
        "content": "there!"
    }
]
```

#### Get Document

```http
GET /ops/{docId}/document
```

| Parameter | Type     | Description                       |
| :-------- | :------- | :-------------------------------- |
| `ts`      | `string` | **Optional**. Timestamp to get document history (eg: 2025-08-17T14:24:17.761Z) |
| `cached`      | `boolean` | **Optional**. Pass `true` to get cached aggregated document content. Default `false` |

#### Get Document Activity

```http
GET /ops/{docId}/user/{userId}
```





## Run Backend Locally

NOTE
- You are running this project as a developer
- Intall IntelliJ IDEA / Eclipse IDE
- Install Doccker Desktop
- Shut down PostgreSQL server if running on local machine
- Shut down MongoDB server if running on local machine
- Ensure the following ports are free `27017`, `5432` and `8080`

Clone the project

```bash
  git clone https://github.com/its-just-pritam/quickbook.git
```

Go to the project directory

```bash
  cd quickbook
```

Build docker images

```bash
  docker compose up --build
```

Start the aggregator application using preferred IDE for Spring Boot.


## Backend Architecture
![Logo](https://github.com/its-just-pritam/quickbook/blob/main/backend-design.png)


## Authors

- [Pritam Mallick](https://www.linkedin.com/in/pritammallick20/)
- [Sarthak Johnson Prasad](https://www.linkedin.com/in/sarthak-johnson-prasad-203702182/)

