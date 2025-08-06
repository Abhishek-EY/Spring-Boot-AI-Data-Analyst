AI-Driven Business Analyst with MongoDB

This project is a Spring Boot + MongoDB + Gemini AI proof of concept that converts natural language business questions into MongoDB aggregation queries, executes them, and returns business-friendly insights.

🚀 Features
    Natural Language to MongoDB Query:
      Ask questions like "Which product performed the best?", and the AI generates a MongoDB aggregation pipeline automatically.
    
    Automatic Query Retry:
      If MongoDB throws an error, the system retries with a corrected aggregation.
    
    Business-Friendly Insights:
      AI interprets query results into clear, human-readable explanations.
    
    Schema-Aware Queries:
      AI is given the database schema to produce more accurate queries.

🛠 Tech Stack
    Backend: Spring Boot (Java), Spring Data MongoDB, RestTemplate
    
    Database: MongoDB
    
    AI Model: Google Gemini API
    
    Frontend: HTML, CSS, JavaScript, Marked.js
    
    Build Tool: Maven
    
    Version Control: Git

⚙️ Setup Instructions

1️⃣ Clone Repository
    bash
    Copy
    Edit
    git clone https://github.com/yourusername/ai-business-analyst.git
    cd ai-business-analyst
    
2️⃣ Configure application.yml
    yaml
    Copy
    Edit
    spring:
      data:
        mongodb:
          uri: mongodb://localhost:27017/yourdb
    gemini:
      api-key: YOUR_GEMINI_API_KEY(Get this from google ai studio)
      
3️⃣ Run MongoDB
    run a mongodb local
    
4️⃣ Start Spring Boot App
    bash
    Copy
    Edit
    mvn spring-boot:run
    
5️⃣ Access Frontend
    Open http://localhost:8080/index.html in your browser.

💡 How It Works
    User Inputs Prompt → e.g., "Which product performed the best?"
    
    AI Generates MongoDB Aggregation → Gemini returns aggregation JSON.
    
    Backend Executes Query → Runs aggregation on MongoDB.
    
    AI Interprets Results → Generates business-friendly insights.
    
    Frontend Displays Insight → Insight is shown as formatted text.
    
🔮 Future Enhancements
    Add chart visualization for query results.

    
    Multi-language insights.
    
    Export insights to PDF/Excel.
    
    User authentication.
