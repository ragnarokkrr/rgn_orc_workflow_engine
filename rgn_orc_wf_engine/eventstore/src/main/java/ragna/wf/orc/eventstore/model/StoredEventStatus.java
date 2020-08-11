package ragna.wf.orc.eventstore.model;

public enum StoredEventStatus {
  UNPROCESSED,
  PROCESSING,
  PROCESSED,
  PUBLISHED,
  UNPUBLISHED,
  FAILED
}
