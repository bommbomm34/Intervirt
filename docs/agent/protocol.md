# Intervirt Agent API Specification

This document contains the API specification of the *Intervirt Agent* which acts as a server for the Intervirt client.

## WebSocket Endpoints

The client requires one endpoint to be available via unencrypted WebSocket (`ws://`). It should be accessible through
`ws://localhost:55436/containerManagement` via `GET`.

Messages from the client will always be encoded in JSON. Messages from the server will also always be encoded in JSON.
The programm will also send commands to the server. Below, you will see example requests and corresponding example
responses. Please keep in mind that there are some more fields in the requests/responses to fill than the example
requests/responses. See the *Tips* section to find the extra fields.

### Add container

Message from client:

```json
{
    "type": "AddContainer",
    "id": "computer-93281", // Name of the container
    "ipv4": "192.168.0.145", // IPv4 address of the container
    "ipv6": "fd00:3452:2312:ab32:cdaa:4444:aaaa:bcda", // IPv6 address of the container
    "mac": "2a:5b:22:67:67:67", // MAC address of the container
    "internet": false, // Public internet access of the container
    "image": "debian/13" // Image of the container
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 already exists" // Error message
    "code": 3 // Status code of the error
}
```

### Remove container

Message from client:

```json
{
    "type": "RemoveContainer",
    "id": "computer-93281" // Name of the container to remove
}
```

Successful message from server:

```json
{
    "progress": 0.9, // Percentage of progress (0.9 means 90%)
    "output": "Shutting down container..." // Progress message
    "status": -1
}
```

Then:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist." // Error message
    "code": 6
} 
```

### Start container

Message from client:

```json
{
    "type": "StartContainer",
    "id": "computer-93281" // Name of the container to start
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist." // Error message
    "code": 6
}
```

### Stop container

Message from client:

```json
{
    "type": "StopContainer",
    "id": "computer-93281" // Name of the container to stop
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist." // Error message
    "code": 6
}
```

### Set IPv4 address

Message from client:

```json
{
    "type": "IDWithNewIpv4",
    "id": "computer-93281", // Name of the container
    "newIP": "192.168.0.133", // New IPv4 address of the container
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist"
    "code": 6
}
```

### Set IPv6 address

Message from client:

```json
{
    "type": "IDWithNewIpv6",
    "id": "computer-93281", // Name of the container
    "newIP": "fd00:3452:2312:ab32:cdaa:4444:bbbb:bcda", // New IPv6 address of the container
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist"
    "code": 6
}
```

### Connect

Message from client:

```json
{
    "type": "Connect",
    "id1": "computer-93281", // Name of the first container to connect
    "id2": "computer-31222", // Name of the second container to connect
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 and computer-31222 don't exist"
    "code": 6
}
```

You should connect both containers via networking.

### Disconnect

Message from client:

```json
{
    "type": "Disconnect",
    "id1": "computer-93281", // Name of the first container to disconnect
    "id2": "computer-31222", // Name of the second container to disconnect
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 and computer-31222 don't exist"
    "code": 6
}
```

### Set internet access

Message from client:

```json
{
    "type": "SetInternetAccess",
    "id": "computer-93281",
    "enabled": true, // If internet access should be enabled
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist"
    "code": 6
}
```

### Add port forwarding

Message from client:

```json
{
    "type": "AddPortForwarding",
    "id": "computer-93281",
    "internalPort": 80 // Port in the container to forward
    "externalPort": 8080, // External port to forward
    "protocol": "tcp" // Protocol of the port forwarding
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist"
    "code": 6
}
```

### Remove port forwarding

Message from client:

```json
{
    "type": "RemovePortForwarding",
    "externalPort": 8080, // External port of the port forwarding to remove
    "protocol": "tcp" // Protocol of the port forwarding
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "error": "Container computer-93281 doesn't exist",
    "code": 6
}
```

### Wipe

Message from client:

```json
{
    "type": "Command",
    "command": "wipe"
}
```

Successful message from server:

```json
{
    "progress": 0.9,
    "output": "Removing container computer-XXXXX",
    "status": -1
}
```

Then:

```json
{}
```

Failed message from server:

```json
{
    "code": 2 // Status code of error
}
```

### Update

Message from client:

```json
{
    "type": "Command",
    "command": "update"
}
```

Successful message from server:

```json
{
    "progress": 0.9,
    "output": "Updating ssh to X.X.X...",
    "status": -1
}
```

Then:

```json
{}
```

Failed message from server:

```json
{
    "code": 2 // Status code of error
}
```

This command should perform a system upgrade on the guest and also update all containers and restart them.

### Shutdown

Message from client:

```json
{
    "type": "Command",
    "command": "shutdown"
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "code": 2
}
```

This should shutdown all containers and the guest.

### Reboot

Message from client:

```json
{
    "type": "Command",
    "command": "reboot"
}
```

Successful message from server:

```json
{}
```

Failed message from server:

```json
{
    "code": 2
}
```

This should shutdown all containers and reboot the guest.

### Get version

Message from client:

```json
{
    "type": "Command",
    "command": "version"
}
```

Message from server:

```json
{
    "version": "1.2.3" // Version of the Intervirt Agent
}
```

### Intervirt Error Codes

Intervirt has its own error codes:

| Error code | Description                                                                                                        | Error text required |
|------------|--------------------------------------------------------------------------------------------------------------------|---------------------|
| 1          | There is an error available, but it's not defined by the Intervirt error codes explicitly.                         | yes                 |
| 2          | There was an unsuccessful operation, but no error was given.                                                       | no                  |
| 3          | The operation was already performed.                                                                               | no                  |
| 4          | There was an error associated with the guest system.                                                               | yes                 |
| 5          | There was an error in a command execution on the guest system                                                      | yes                 |
| 6          | The requested or needed resource was not found.                                                                    | yes                 |
| 7          | The server doesn't support the operation                                                                           | no                  |
| 8          | The JSON input of the client isn't valid or contains invalid arguments (e.g. invalid protocol for port forwarding) | yes                 |

## Tips

- Some commands in WebSockets require a stream instead a single answer (e.g. `wipe`).
- Every client request in WebSockets will contain the field `type`.
- The server should listen on all interfaces on port 55436
- Keep in mind that every request over WebSockets in JSON will contain a field named ```uuid``` which is just a random
  UUIDv4.
- Send status code ```-1``` explicitly if the task is not completed.
- If no status code is sent, the client will default to the status code ```0```.
- The example responses don't include ```refID```. Please include it in production. It should contain the ```uuid``` of
  the request which the response refers to.
- Always return a ```type``` field in the JSON responses. It should be ```Version``` (if the client requests the
  version), otherwise ```General```

If you have questions, simply contact me and I'll answer you as soon as possible.