package log

import (
	"github.com/Sirupsen/logrus"
	"os"
)

var (
	DebugLevel = logrus.DebugLevel
	InfoLevel  = logrus.InfoLevel
	WarnLevel  = logrus.WarnLevel
	ErrorLevel = logrus.ErrorLevel
	FatalLevel = logrus.FatalLevel
	PanicLevel = logrus.PanicLevel
)

func init() {
	format := new(logrus.TextFormatter)
	format.FullTimestamp = true
	format.TimestampFormat = "2006-01-02 15:04:05"
	logrus.SetFormatter(format)
	logrus.SetLevel(logrus.InfoLevel)
}

// Check provides ability to check error state, write debug information
// and perform action by error level
func Check(level logrus.Level, msg string, err error) bool {
	if err != nil {
		switch level {
		case logrus.PanicLevel:
			logrus.Panic(msg, ", ", err)
		case logrus.FatalLevel:
			logrus.Fatal(msg, ", ", err)
		case logrus.ErrorLevel:
			logrus.Error(msg, ", ", err)
			os.Exit(1)
		case logrus.WarnLevel:
			logrus.Warn(msg, ", ", err)
		case logrus.InfoLevel:
			logrus.Info(msg, ", ", err)
		case logrus.DebugLevel:
			logrus.Debug(msg, ", ", err)
		}
		return true
	}
	logrus.Debug(msg)
	return false
}

// Level sets output level
func Level(level logrus.Level) {
	logrus.SetLevel(level)
}

// Panic stops process after showing panic message. Highest error level
func Panic(msg ...string) {
	logrus.Panic(msg)
}

// Fatal stops process after showing fatal message.
func Fatal(msg ...string) {
	logrus.Fatal(msg)
}

// Error stops process after showing error message.
func Error(msg ...string) {
	logrus.Error(msg)
	os.Exit(1)
}

// Warn keeps process working after showing warning message.
func Warn(msg ...string) {
	logrus.Warn(msg)
}

// Info keeps process working after showing information message.
func Info(msg ...string) {
	logrus.Info(msg)
}

// Debug logs debug information
func Debug(msg ...string) {
	logrus.Debug(msg)
}
