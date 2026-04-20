import { useState, useEffect } from "react";
import "./App.css";

function App() {
    const [darkMode, setDarkMode] = useState(false);

  const [tokenId, setTokenId] = useState("");
  const [servedToken, setServedToken] = useState(null);
  const [message, setMessage] = useState("");
  const [queueSize, setQueueSize] = useState(0);

  // 🌙 Toggle theme
  const toggleTheme = () => {
    setDarkMode(!darkMode);
  };

  // Apply class to body
  useEffect(() => {
    document.body.className = darkMode ? "dark" : "";
  }, [darkMode]);

  // 🔁 Fetch queue size
  const fetchQueueSize = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/queue/size");
      const text = await res.text();

      const num = Number(text);
      if (!isNaN(num)) {
        setQueueSize(num);
      }
    } catch {
      console.log("Error fetching queue size");
    }
  };

  useEffect(() => {
    fetchQueueSize();
    const interval = setInterval(fetchQueueSize, 3000);
    return () => clearInterval(interval);
  }, []);

  useEffect(() => {
  const resetAndFetch = async () => {
    await fetch("http://localhost:8080/api/queue/reset", {
      method: "POST",
    });

    fetchQueueSize(); // 👈 refresh UI after reset
    setTokenId("");
    setServedToken(null);
    setMessage("");
  };

  resetAndFetch();
}, []);

  // 🎟️ Create Token
  const createToken = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/queue/token", {
        method: "POST",
      });

      const id = await res.text();

      // fetch full token details
      const tokenRes = await fetch(
        `http://localhost:8080/api/queue/token/${id}`
      );
      const tokenData = await tokenRes.json();

      setTokenId(tokenData.tokenNumber); // 👈 show short number
      setMessage("Token created successfully");
      setServedToken(null);

      fetchQueueSize();
    } catch {
      setMessage("Error creating token");
    }
  };

  // 🧾 Serve Token
  const serveToken = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/queue/serve", {
        method: "POST",
      });

      const text = await res.text();

      if (text === "Queue is empty") {
        setMessage("Queue is empty");
        setServedToken(null);
        return;
      }

      try {
        const data = JSON.parse(text);
        setServedToken(data);
        setMessage("Token served successfully");
        setTokenId(""); // clear previous created token
        fetchQueueSize();
      } catch {
        setMessage(text);
      }
    } catch {
      setMessage("Error serving token");
    }
  };

  return (
    <div className="container">
      {/* 🌙 Toggle Button */}
      <button className="toggle" onClick={toggleTheme}>
        {darkMode ? "☀️ Light Mode" : "🌙 Dark Mode"}
      </button>
      <h1>Smart Queue Management System</h1>

      {/* Queue Status */}
      <div className="card">
        <h2>Queue Status</h2>
        <p className="queue">Queue Size: {queueSize}</p>
      </div>

      {/* Create Token */}
      <div className="card">
        <h2>Create Token</h2>
        <button onClick={createToken}>Create Token</button>

        {tokenId && (
          <p>
            <b>Token:</b> #{tokenId}
          </p>
        )}
      </div>

      {/* Serve Token */}
      <div className="card">
        <h2>Serve Token</h2>
        <button onClick={serveToken}>Serve Next</button>

        {servedToken && (
          <div className="token-box">
            <p>
              <b>Token:</b> #{servedToken.tokenNumber}
            </p>
            <p>
              <b>Status:</b> {servedToken.status}
            </p>
          </div>
        )}
      </div>

      {/* Message */}
      <p className="message">
        {typeof message === "string" ? message : ""}
      </p>
    </div>
  );
}

export default App;