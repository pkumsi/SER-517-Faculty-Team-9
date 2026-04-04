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

// IncrementMessageCount increments the daily message count for the current day
func (r *RedisCache) IncrementMessageCount(ctx context.Context) error {
	today := time.Now().Format("2006-01-02")
	key := fmt.Sprintf("stats:messages:%s", today)
	return r.client.Incr(ctx, key).Err()
}

// GetDailyMessageCounts returns message counts for the last N days
func (r *RedisCache) GetDailyMessageCounts(ctx context.Context, days int) (map[string]int64, error) {
	result := make(map[string]int64)
	
	for i := 0; i < days; i++ {
		date := time.Now().AddDate(0, 0, -i).Format("2006-01-02")
		key := fmt.Sprintf("stats:messages:%s", date)
		
		count, err := r.client.Get(ctx, key).Int64()
		if err != nil && err != redis.Nil {
			return nil, err
		}
		result[date] = count
	}
	
	return result, nil
}
