document.getElementById('promptForm').addEventListener('submit', async function (e) {
  e.preventDefault();
  const prompt = document.getElementById('prompt').value.trim();
  const responseBox = document.getElementById('responseBox');
  const responseText = document.getElementById('responseText');

  responseBox.style.display = 'block';
  responseText.textContent = '💡 Generating insight';
  responseText.classList.add('loading');

  try {
    const res = await fetch('http://localhost:8080/api/dataAnalyst/analyse', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ prompt })
    });

    if (!res.ok) throw new Error("Something went wrong");

    const data = await res.text();
    responseText.classList.remove('loading');
    const safeHtml = DOMPurify.sanitize(marked.parse(data));
    responseText.innerHTML = safeHtml;

  } catch (err) {
    responseText.textContent = 'Failed to generate insight. Please try again.';
    console.error(err);
  }
});
