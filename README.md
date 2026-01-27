# **webhook-external-secret**-credentials-provider

## Introduction

Define a new credentials store to store incoming webhook external secrets.

This could be useful to integrate with external secret management systems that can send
secrets via webhooks to Jenkins.

Only works for Global credentials. From UI point of view it's only possible to delete credentials.

Incoming webhooks are protected by a shared token and must be sent via Bearer authentication.

## Getting started

The payload to send to the webhook endpoint is a JSON object with the following structure:

If secret already exists it's just updated.

For a StringCredentials

```json
{
  "id": "my-secret-1", 
    "description": "My first secret",
    "type": "token",
    "secret": {
      "token": "123456"
    }
}
```
For UsernamePasswordCredentials

```json
{
  "id": "my-secret-2", 
    "description": "My second secret",
    "type": "usernamePassword",
    "secret": {
      "username": "myuser",
      "password": "mypassword"
    }
}

## LICENSE

Licensed under MIT, see [LICENSE](LICENSE.md)

