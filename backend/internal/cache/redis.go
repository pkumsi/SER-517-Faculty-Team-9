package cache

import (
	"context"
	"crypto/sha256"
	"fmt"
	"time"

	"github.com/redis/go-redis/v9"
)

type RedisCache struct {
	client *redis.Client
	ttl    time.Duration
}

func NewRedisCache(addr, password string, db int, ttl time.Duration) (*RedisCache, error) {
	client := redis.NewClient(&redis.Options{
		Addr:     addr,
		Password: password,
		DB:       db,
	})

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if err := client.Ping(ctx).Err(); err != nil {
		client.Close()
		return nil, fmt.Errorf("redis ping failed: %w", err)
	}

	return &RedisCache{client: client, ttl: ttl}, nil
}

func BuildKey(activity, currentTime, senderRole, urgency, expectedResponseTime, variant string) string {
	h := sha256.New()
	for _, part := range []string{activity, currentTime, senderRole, urgency, expectedResponseTime, variant} {
		h.Write([]byte(part))
		h.Write([]byte("|"))
	}
	return fmt.Sprintf("llm:response:%x", h.Sum(nil))
}

// BuildKeyFromRaw builds a cache key by hashing an arbitrary string (e.g. a
// JSON-serialized context snapshot). Used when no inference layer is present.
func BuildKeyFromRaw(data string) string {
	h := sha256.New()
	h.Write([]byte(data))
	return fmt.Sprintf("llm:response:%x", h.Sum(nil))
}

func (r *RedisCache) Get(ctx context.Context, key string) (string, bool) {
	val, err := r.client.Get(ctx, key).Result()
	if err != nil {
		return "", false
	}
	return val, true
}

func (r *RedisCache) Set(ctx context.Context, key, value string) error {
	return r.client.Set(ctx, key, value, r.ttl).Err()
}

func (r *RedisCache) Close() error {
	return r.client.Close()
}
