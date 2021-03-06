(ns com.draines.postal.sendmail
  (:use [com.draines.postal.message :only [message->str sender recipients]]))

(def sendmails ["/usr/lib/sendmail"
                "/usr/sbin/sendmail"
                "/usr/bin/sendmail"
                "/usr/local/lib/sendmail"
                "/usr/local/sbin/sendmail"
                "/usr/local/bin/sendmail"
                "/usr/sbin/msmtp"])

(def errors {0  [:SUCCESS        "message sent"]
             64 [:EX_USAGE       "command line usage error"]
             65 [:EX_DATAERR     "data format error"]
             66 [:EX_NOINPUT     "cannot open input"]
             67 [:EX_NOUSER      "addressee unknown"]
             68 [:EX_NOHOST      "host name unknown"]
             69 [:EX_UNAVAILABLE "service unavailable"]
             70 [:EX_SOFTWARE    "internal software error"]
             71 [:EX_OSERR       "system error (no fork?)"]
             72 [:EX_OSFILE      "critical OS file missing"]
             73 [:EX_CANTCREAT   "can't create (user) output file"]
             74 [:EX_IOERR       "input/output error"]
             75 [:EX_TEMPFAIL    "temp failure; user is invited to retry"]
             76 [:EX_PROTOCOL    "remote error in protocol"]
             77 [:EX_NOPERM      "permission denied"]
             78 [:EX_CONFIG      "configuration error"]})

(defn error [code]
  (let [[e message] (errors code)]
    {:code code
     :error e
     :message message}))

(defn sendmail-find []
  (first (filter #(.isFile (java.io.File. %)) sendmails)))

(defn sanitize [text]
  (.replaceAll text "\r\n" (System/getProperty "line.separator")))

(defn sendmail-send [msg]
  (let [mail (sanitize (message->str msg))
        cmd (concat
             [(sendmail-find) (format "-f %s" (sender msg))]
             (recipients msg))
        pb (ProcessBuilder. cmd)
        p (.start pb)
        smtp (java.io.PrintStream. (.getOutputStream p))]
    (.print smtp mail)
    (.close smtp)
    (.waitFor p)
    (error (.exitValue p))))

