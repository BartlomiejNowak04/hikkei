package dev.tfkls.hikkei.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import org.jetbrains.annotations.NotNull;

public class BoundedMap<K, V> {
  public BoundedMap(int capacity) {
    this(capacity, new HashMap<>(), new ArrayBlockingQueue<>(capacity));
  }

  public BoundedMap(int capacity, Map<K, V> mapImpl, Queue<K> queueImpl) {
    assert capacity > 0;
    this.capacity = capacity;
    map = mapImpl;
    queue = queueImpl;
  }

  private final int capacity;
  private final Map<K, V> map;
  private final Queue<K> queue;

  public void put(@NotNull K key, @NotNull V value) {
    map.put(key, value);
    if (queue.size() == capacity) {
      map.remove(queue.remove());
    }
    queue.add(key);
  }

  public Optional<V> get(@NotNull K key) {
    return Optional.ofNullable(map.get(key));
  }

  public Optional<V> remove(@NotNull K key) {
    return Optional.ofNullable(map.remove(key));
  }
}
