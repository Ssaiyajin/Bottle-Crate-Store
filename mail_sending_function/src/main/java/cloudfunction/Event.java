package cloudfunction;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public class Event {
    private String bucket;
    private String name;
    private String metageneration;
    private Instant timeCreated;
    private Instant updated;
    private Map<String, String> metadata;

    public Event() {
    }

    public Event(String bucket, String name, String metageneration, Instant timeCreated, Instant updated, Map<String, String> metadata) {
        this.bucket = bucket;
        this.name = name;
        this.metageneration = metageneration;
        this.timeCreated = timeCreated;
        this.updated = updated;
        this.metadata = metadata;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMetageneration() {
        return metageneration;
    }

    public void setMetageneration(String metageneration) {
        this.metageneration = metageneration;
    }

    public Instant getTimeCreated() {
        return timeCreated;
    }

    /**
     * Set timeCreated using an Instant.
     */
    public void setTimeCreated(Instant timeCreated) {
        this.timeCreated = timeCreated;
    }

    /**
     * Convenience setter that accepts ISO-8601 string (e.g. "2020-01-01T00:00:00Z").
     * If the value is null or blank, the field will be set to null.
     */
    public void setTimeCreated(String timeCreated) {
        this.timeCreated = (timeCreated == null || timeCreated.isBlank()) ? null : Instant.parse(timeCreated);
    }

    public Instant getUpdated() {
        return updated;
    }

    public void setUpdated(Instant updated) {
        this.updated = updated;
    }

    public void setUpdated(String updated) {
        this.updated = (updated == null || updated.isBlank()) ? null : Instant.parse(updated);
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;
        return Objects.equals(bucket, event.bucket) &&
               Objects.equals(name, event.name) &&
               Objects.equals(metageneration, event.metageneration) &&
               Objects.equals(timeCreated, event.timeCreated) &&
               Objects.equals(updated, event.updated) &&
               Objects.equals(metadata, event.metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bucket, name, metageneration, timeCreated, updated, metadata);
    }

    @Override
    public String toString() {
        return "Event{" +
               "bucket='" + bucket + '\'' +
               ", name='" + name + '\'' +
               ", metageneration='" + metageneration + '\'' +
               ", timeCreated=" + timeCreated +
               ", updated=" + updated +
               ", metadata=" + metadata +
               '}';
    }
}
