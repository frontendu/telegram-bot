package postgres

import (
	"database/sql"
	"time"
	"errors"

	"github.com/frontendu/telegram-bot/services/core/pkg/logger"

	_ "github.com/lib/pq"
)

type DB struct {
	Session *sql.DB
	Logger  logger.Logger
}

type Config struct {
	MaxConnLifetime time.Duration
	MaxIdleConns    int
	MaxOpenConns    int
}

func New(dsn string, logger logger.Logger, cfg Config) (*DB, error) {
	db, err := sql.Open("postgres", dsn)
	if err != nil {
		return nil, errors.New("could not open database")
	}
	db.SetConnMaxLifetime(cfg.MaxConnLifetime)
	db.SetMaxIdleConns(cfg.MaxIdleConns)
	db.SetMaxOpenConns(cfg.MaxOpenConns)

	return &DB{
		Session: db,
		Logger:  logger,
	}, nil
}

func (d *DB) Connect() error {
	var err error
	maxAttempts := 10
	for attempt := 1; attempt <= maxAttempts; attempt++ {
		err = d.Session.Ping()
		if err == nil {
			break
		}
		nextAttemptWait := time.Duration(attempt) * time.Second
		d.Logger.Warnf("attempt %v: could not establish a connection with the database, wait for %v.", attempt, nextAttemptWait)
		time.Sleep(nextAttemptWait)
	}
	if err != nil {
		return errors.New("could not connect to database")
	}
	return nil
}

func (d *DB) Close() error {
	if err := d.Session.Close(); err != nil {
		return errors.New("could not close database")
	}
	return nil
}
