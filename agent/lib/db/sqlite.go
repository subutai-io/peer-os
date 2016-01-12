package db

import (
	"database/sql"
	_ "github.com/mattn/go-sqlite3"
	"os"
	"subutai/config"
	"subutai/log"
)

func Init() {
	if _, err := os.Stat(config.Agent.LxcPrefix + "subutai.db"); err != nil {
		sqlite, err := sql.Open("sqlite3", config.Agent.LxcPrefix+"subutai.db")
		log.Check(log.FatalLevel, "Creationg database", err)
		defer sqlite.Close()

		query := `create table uid (uid integer primary key not null, container text);`
		_, err = sqlite.Exec(query)
		log.Check(log.FatalLevel, "Creating table", err)

		_, err = sqlite.Exec("insert into uid(uid, container) values(65536, NULL)")
		log.Check(log.FatalLevel, "Inserting initial values", err)

	}
}

func GetUid(name string) int {
	var uid int
	sqlite, err := sql.Open("sqlite3", config.Agent.LxcPrefix+"subutai.db")
	log.Check(log.FatalLevel, "Opening database", err)
	defer sqlite.Close()

	rows, err := sqlite.Query("select uid from uid where container is NULL limit 1")
	log.Check(log.FatalLevel, "Getting free UID", err)
	defer rows.Close()

	for rows.Next() {
		rows.Scan(&uid)
	}

	return uid
}

// func ss(query string) *sql.Rows {
// 	database, err := sql.Open("sqlite3", config.Agent.LxcPrefix+"subutai.db")
// 	log.Check(log.FatalLevel, "Creating database", err)
// 	defer database.Close()

// 	rows, err := database.Query(query)
// 	log.Check(log.FatalLevel, "Running query "+query, err)
// 	defer rows.Close()
// 	// log.Check(log.FatalLevel, "Getting response", rows.Scan(&output))
// 	return rows
// }
