package com.chatBot.service;

import java.nio.ByteBuffer;
import java.util.Comparator;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ChunkStore {

    private final JdbcTemplate jdbc;

    public ChunkStore(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record Hit(String content, Integer page, double score) {}

    public void save(String docId, String fileName, Integer page, String content, float[] emb) {
        jdbc.update(
            "INSERT INTO doc_chunk(doc_id, file_name, page_number, content, embedding) VALUES(?,?,?,?,?)",
            docId, fileName, page, content, toBytes(emb));
    }

    public List<Hit> search(String docId, float[] query, int topK, double threshold) {
        List<Hit> hits = jdbc.query(
            "SELECT content, page_number, embedding FROM doc_chunk WHERE doc_id = ?",
            (rs, i) -> new Hit(
                rs.getString(1),
                (Integer) rs.getObject(2),
                cosine(query, toFloats(rs.getBytes(3)))),
            docId);

        return hits.stream()
            .filter(h -> h.score() >= threshold)
            .sorted(Comparator.comparingDouble(Hit::score).reversed())
            .limit(topK)
            .toList();
    }

    private static byte[] toBytes(float[] f) {
        ByteBuffer b = ByteBuffer.allocate(f.length * 4);
        for (float v : f) b.putFloat(v);
        return b.array();
    }

    private static float[] toFloats(byte[] bytes) {
        ByteBuffer b = ByteBuffer.wrap(bytes);
        float[] f = new float[bytes.length / 4];
        for (int i = 0; i < f.length; i++) f[i] = b.getFloat();
        return f;
    }

    private static double cosine(float[] a, float[] b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }
}