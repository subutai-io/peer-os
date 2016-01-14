package lib

import (
	"io"
	"os"
	"subutai/log"
)

func CopyFile(source string, dest string) {
	sf, err := os.Open(source)
	log.Check(log.FatalLevel, "Opening file "+source, err)
	defer sf.Close()

	df, err := os.Create(dest)
	log.Check(log.FatalLevel, "Creating file "+dest, err)
	defer df.Close()

	_, err = io.Copy(df, sf)
	log.Check(log.FatalLevel, "Coping file "+source+" to "+dest, err)
}
