# WebSocket 채팅 연결 가이드

## 빠른 시작

### 1단계: 티켓 발급

```bash
curl -X POST https://api.takealook.app/chat/ticket \
  -H "accessToken: YOUR_JWT_TOKEN"
```

**응답:**
```json
{
  "ticket": "550e8400-e29b-41d4-a716-446655440000",
  "expiresIn": 30
}
```

### 2단계: WebSocket 연결

```javascript
const ticket = "550e8400-e29b-41d4-a716-446655440000";
const ws = new WebSocket(`wss://api.takealook.app/chat?ticket=${ticket}`);

ws.onopen = () => {
  console.log("Connected!");
};

ws.onmessage = (event) => {
  const message = JSON.parse(event.data);
  console.log("Received:", message);
};

ws.onerror = (error) => {
  console.error("WebSocket error:", error);
};
```

## 클라이언트별 구현 예시

### JavaScript (Browser)

```javascript
class ChatClient {
  constructor(baseUrl, accessToken) {
    this.baseUrl = baseUrl;
    this.accessToken = accessToken;
    this.ws = null;
  }

  async connect() {
    const response = await fetch(`${this.baseUrl}/chat/ticket`, {
      method: 'POST',
      headers: { 'accessToken': this.accessToken }
    });
    const { ticket } = await response.json();

    const wsUrl = this.baseUrl.replace('http', 'ws');
    this.ws = new WebSocket(`${wsUrl}/chat?ticket=${ticket}`);
    
    return new Promise((resolve, reject) => {
      this.ws.onopen = () => resolve(this);
      this.ws.onerror = reject;
    });
  }

  send(message) {
    this.ws.send(JSON.stringify(message));
  }

  onMessage(callback) {
    this.ws.onmessage = (event) => {
      callback(JSON.parse(event.data));
    };
  }

  disconnect() {
    this.ws?.close();
  }
}

const chat = new ChatClient('https://api.takealook.app', accessToken);
await chat.connect();
chat.onMessage((msg) => console.log(msg));
chat.send({ roomId: 1, content: "Hello!" });
```

### Kotlin (Android)

```kotlin
class ChatClient(
    private val baseUrl: String,
    private val accessToken: String
) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val ticketRequest = Request.Builder()
                .url("$baseUrl/chat/ticket")
                .post("".toRequestBody())
                .header("accessToken", accessToken)
                .build()

            val ticketResponse = client.newCall(ticketRequest).execute()
            val ticket = json.decodeFromString<WsTicket>(
                ticketResponse.body?.string() ?: throw Exception("Empty response")
            )

            val wsUrl = baseUrl.replace("https", "wss")
                .replace("http", "ws")
            val wsRequest = Request.Builder()
                .url("$wsUrl/chat?ticket=${ticket.ticket}")
                .build()

            webSocket = client.newWebSocket(wsRequest, ChatWebSocketListener())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun send(message: ChatMessage) {
        webSocket?.send(json.encodeToString(message))
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
    }
}
```

### Swift (iOS)

```swift
class ChatClient {
    private let baseURL: String
    private let accessToken: String
    private var webSocket: URLSessionWebSocketTask?
    
    init(baseURL: String, accessToken: String) {
        self.baseURL = baseURL
        self.accessToken = accessToken
    }
    
    func connect() async throws {
        var request = URLRequest(url: URL(string: "\(baseURL)/chat/ticket")!)
        request.httpMethod = "POST"
        request.setValue(accessToken, forHTTPHeaderField: "accessToken")
        
        let (data, _) = try await URLSession.shared.data(for: request)
        let ticket = try JSONDecoder().decode(WsTicket.self, from: data)
        
        let wsURL = baseURL
            .replacingOccurrences(of: "https", with: "wss")
            .replacingOccurrences(of: "http", with: "ws")
        let url = URL(string: "\(wsURL)/chat?ticket=\(ticket.ticket)")!
        
        webSocket = URLSession.shared.webSocketTask(with: url)
        webSocket?.resume()
        
        receiveMessages()
    }
    
    private func receiveMessages() {
        webSocket?.receive { [weak self] result in
            switch result {
            case .success(let message):
                self?.receiveMessages()
            case .failure(let error):
                print("WebSocket error: \(error)")
            }
        }
    }
    
    func send(_ message: ChatMessage) throws {
        let data = try JSONEncoder().encode(message)
        webSocket?.send(.data(data)) { error in
            if let error = error {
                print("Send error: \(error)")
            }
        }
    }
    
    func disconnect() {
        webSocket?.cancel(with: .goingAway, reason: nil)
    }
}
```

## 메시지 형식

### 보내는 메시지 (Client → Server)

```json
{
  "roomId": 1,
  "content": "안녕하세요!",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### 받는 메시지 (Server → Client)

```json
{
  "id": 123,
  "roomId": 1,
  "content": "안녕하세요!",
  "timestamp": "2024-01-15T10:30:00Z",
  "user": {
    "id": 1,
    "username": "john",
    "profileImage": "https://..."
  }
}
```

## 에러 처리

### 연결 실패 코드

| Close Code | 의미 | 대응 |
|------------|------|------|
| 1002 (POLICY_VIOLATION) | 티켓 누락 또는 Origin 거부 | 티켓 재발급 후 재연결 |
| 1003 (NOT_ACCEPTABLE) | 티켓 만료/무효 | 티켓 재발급 후 재연결 |
| 1011 (SERVER_ERROR) | 서버 오류 | 잠시 후 재연결 |

### 재연결 로직 권장

```javascript
class ReconnectingChatClient extends ChatClient {
  async connectWithRetry(maxRetries = 3) {
    for (let i = 0; i < maxRetries; i++) {
      try {
        await this.connect();
        return;
      } catch (error) {
        console.log(`Connection attempt ${i + 1} failed`);
        await new Promise(r => setTimeout(r, 1000 * (i + 1)));
      }
    }
    throw new Error("Failed to connect after retries");
  }
}
```

## FAQ

### Q: 티켓이 만료되면 어떻게 하나요?
티켓은 30초간만 유효합니다. 만료 시 `/chat/ticket`을 다시 호출하여 새 티켓을 발급받으세요.

### Q: 연결이 끊어지면 자동 재연결되나요?
서버는 자동 재연결을 지원하지 않습니다. 클라이언트에서 재연결 로직을 구현해야 합니다.

### Q: 동시에 여러 기기에서 연결할 수 있나요?
네, 동일 사용자가 여러 기기에서 동시 연결 가능합니다. 각 연결마다 별도 티켓이 필요합니다.
