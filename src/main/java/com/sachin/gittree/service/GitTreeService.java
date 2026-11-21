
package com.sachin.gittree.service;

import com.sachin.gittree.dto.TreeNodeDto;
import com.sachin.gittree.dto.TreeRequestDto;
import com.sachin.gittree.dto.TreeResponseDto;
import com.sachin.gittree.exception.InvalidGitHubUrlException;
import com.sachin.gittree.exception.RepositoryNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.*;

@Service
public class GitTreeService {

    private final RestClient restClient =
            RestClient.create("https://api.github.com");

    // Internal tree node
    private static class Node {
        String name;
        boolean isFolder;
        long size;
        Map<String, Node> children = new LinkedHashMap<>();

        Node(String name, boolean isFolder, long size) {
            this.name = name;
            this.isFolder = isFolder;
            this.size = size;
        }
    }

    public TreeResponseDto generateTree(TreeRequestDto request) {

        // 1. Parse repository URL
        String[] repoDetails = parseGitHubUrl(request.getRepoUrl());

        String owner = repoDetails[0];
        String repo = repoDetails[1];

        String branch = (request.getBranch() == null ||
                request.getBranch().isBlank())
                ? "main"
                : request.getBranch().trim();

        // 2. Fetch repository tree
        Map<String, Object> response =
                fetchFromGitHub(owner, repo, branch);

        List<Map<String, Object>> files =
                (List<Map<String, Object>>) response.get("tree");

        if (files == null) {
            throw new RepositoryNotFoundException(
                    "No files found in repository: " + owner + "/" + repo);
        }

        // 3. Build tree
        Node root = new Node(repo, true, 0);

        for (Map<String, Object> file : files) {

            String path = (String) file.get("path");
            String type = (String) file.get("type");

            long size = file.get("size") == null
                    ? 0
                    : ((Number) file.get("size")).longValue();

            String[] parts = path.split("/");

            Node current = root;

            for (int i = 0; i < parts.length; i++) {

                String name = parts[i];
                boolean isLast = i == parts.length - 1;

                boolean isFolder = !isLast || "tree".equals(type);

                long nodeSize = (!isFolder && isLast) ? size : 0;

                current.children.putIfAbsent(
                        name,
                        new Node(name, isFolder, nodeSize)
                );

                current = current.children.get(name);
            }
        }

        // 4. Calculate sizes and sort children
        calculateSize(root);

        // 5. Generate DTO and Markdown output
        List<TreeNodeDto> nodes = new ArrayList<>();

        String totalSize = formatBytes(root.size);

        StringBuilder markdown = new StringBuilder();

        markdown.append("```text\n");
        markdown.append(repo)
                .append("/ (")
                .append(totalSize)
                .append(")\n");

        buildTree(root, "", nodes, markdown);

        markdown.append("```");

        return new TreeResponseDto(
                repo,
                branch,
                totalSize,
                nodes,
                markdown.toString()
        );
    }

    // Calculate folder sizes recursively and sort children
    private long calculateSize(Node node) {

        if (!node.isFolder) {
            return node.size;
        }

        long total = 0;

        List<Node> sortedChildren =
                new ArrayList<>(node.children.values());

        sortedChildren.sort((a, b) -> {

            if (a.isFolder != b.isFolder) {
                return a.isFolder ? -1 : 1;
            }

            return a.name.compareToIgnoreCase(b.name);
        });

        node.children.clear();

        for (Node child : sortedChildren) {

            node.children.put(child.name, child);

            total += calculateSize(child);
        }

        node.size = total;

        return total;
    }

    // Generate DTO and visual tree recursively
    private void buildTree(
            Node parent,
            String indent,
            List<TreeNodeDto> nodes,
            StringBuilder markdown) {

        List<Node> children =
                new ArrayList<>(parent.children.values());

        for (int i = 0; i < children.size(); i++) {

            Node child = children.get(i);

            boolean isLast = i == children.size() - 1;

            String prefix = indent + (isLast ? "└── " : "├── ");

            String name = child.isFolder
                    ? child.name + "/"
                    : child.name;

            String size = formatBytes(child.size);

            nodes.add(new TreeNodeDto(
                    nodes.size() + 1,
                    prefix,
                    name,
                    child.isFolder ? "tree" : "blob",
                    size
            ));

            markdown.append(prefix)
                    .append(name)
                    .append("  [")
                    .append(size)
                    .append("]\n");

            if (child.isFolder) {

                String nextIndent = indent +
                        (isLast ? "    " : "│   ");

                buildTree(child, nextIndent, nodes, markdown);
            }
        }
    }

    // Fetch repository data from GitHub
    private Map<String, Object> fetchFromGitHub(
            String owner,
            String repo,
            String branch) {

        try {

            return restClient.get()
                    .uri(
                            "/repos/{owner}/{repo}/git/trees/{branch}?recursive=1",
                            owner,
                            repo,
                            branch
                    )
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "GitTree-App")
                    .retrieve()
                    .body(Map.class);

        } catch (HttpClientErrorException.NotFound e) {

            throw new RepositoryNotFoundException(
                    "Repository '" + owner + "/" + repo +
                            "' or branch '" + branch + "' not found."
            );
        }
    }

    // Extract owner and repository name from URL
    private String[] parseGitHubUrl(String url) {

        String cleaned = url.trim()
                .replaceFirst("^https?://", "")
                .replaceFirst("^www\\.", "");

        if (!cleaned.startsWith("github.com/")) {
            throw new InvalidGitHubUrlException(
                    "Invalid GitHub URL."
            );
        }

        String path = cleaned.substring("github.com/".length())
                .replaceFirst("\\.git$", "")
                .replaceAll("/+$", "");

        String[] parts = path.split("/");

        if (parts.length != 2 ||
                parts[0].isBlank() ||
                parts[1].isBlank()) {

            throw new InvalidGitHubUrlException(
                    "Expected URL: https://github.com/owner/repo"
            );
        }

        return parts;
    }

    // Convert bytes into readable format
    private String formatBytes(long bytes) {

        if (bytes < 1024) {
            return bytes + " B";
        }

        double size = bytes / 1024.0;

        if (size < 1024) {
            return String.format("%.2f KB", size);
        }

        size /= 1024;

        if (size < 1024) {
            return String.format("%.2f MB", size);
        }

        return String.format("%.2f GB", size / 1024);
    }
}
