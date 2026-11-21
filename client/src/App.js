import React, { useState } from 'react';
import './App.css';

function App() {
  const [repoUrl, setRepoUrl] = useState('');
  const [branch, setBranch] = useState('main');
  const [treeData, setTreeData] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleGenerate = async (e) => {
    e.preventDefault();
    setError('');
    setTreeData(null);
    setCopied(false);
    setLoading(true);

    try {
      const response = await fetch('http://localhost:8080/api/tree', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ repoUrl, branch }),
      });

      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.message || 'Failed to fetch repository tree');
      }

      setTreeData(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleCopyMarkdown = () => {
    if (!treeData) return;
    navigator.clipboard.writeText(treeData.markdown);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
      <div className="app-container">
        <div className="header">
          <h1>GitTree</h1>
          <p>Inspect public GitHub repository directory trees with file and folder sizes</p>
        </div>

        <form className="search-form" onSubmit={handleGenerate}>
          <input
              type="text"
              className="url-input"
              placeholder="https://github.com/owner/repo"
              value={repoUrl}
              onChange={(e) => setRepoUrl(e.target.value)}
              required
          />
          <input
              type="text"
              className="branch-input"
              placeholder="Branch (default: main)"
              value={branch}
              onChange={(e) => setBranch(e.target.value)}
          />
          <button type="submit" className="btn" disabled={loading}>
            {loading ? 'Loading...' : 'Generate Tree'}
          </button>
        </form>

        {error && <div className="error-box">{error}</div>}

        {treeData && (
            <div className="tree-card">
              <div className="tree-header">
            <span>
              <strong>{treeData.repoName}/</strong> ({treeData.branch}) &mdash; Total Size:{' '}
              <strong>{treeData.totalFormattedSize}</strong>
            </span>
                <button type="button" className="btn-copy" onClick={handleCopyMarkdown}>
                  {copied ? 'Copied!' : 'Copy Markdown'}
                </button>
              </div>

              <div className="tree-body">
                <div className="tree-row">
                  <span className="tree-left folder-name">{treeData.repoName}/</span>
                  <span className="tree-size">{treeData.totalFormattedSize}</span>
                </div>

                {treeData.treeContents.map((item) => (
                    <div key={item.id} className="tree-row">
                <span className="tree-left">
                  <span className="tree-prefix">{item.prefix}</span>
                  <span className={item.type === 'tree' ? 'folder-name' : 'file-name'}>
                    {item.text}
                  </span>
                </span>
                      <span className="tree-size">{item.formattedSize}</span>
                    </div>
                ))}
              </div>
            </div>
        )}
      </div>
  );
}

export default App;