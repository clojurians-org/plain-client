(defproject plain-client "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-thriftc "0.2.3"]
            [lein-protobuf "0.4.0"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.apache.thrift/libthrift "0.10.0"]
                 [com.google.protobuf/protobuf-java "2.5.0"]]
  :hooks [leiningen.thriftc]
  :thriftc {:path "thrift"
            :source-paths ["src/thrift"]
            :target-path "src"}
  :proto-path "src/proto"
  :java-source-paths ["src/java" "src/gen-java"]
  :source-paths ["src/clojure"])
