(ns plain-client.thrift.hive)

(import '[javax.security.auth.callback NameCallback PasswordCallback UnsupportedCallbackException])

(import '[org.apache.thrift.transport TSocket TSaslClientTransport])
(import '[org.apache.thrift.protocol TBinaryProtocol])

(import '[org.apache.hive.service.rpc.thrift TCLIService$Client TProtocolVersion 
                                             TOpenSessionReq TExecuteStatementReq TGetResultSetMetadataReq TCloseOperationReq])

(defn open 
  ([hostname] (open hostname 10000 nil nil 8))
  ([hostname port username password api-version]
    (let [transport (new TSaslClientTransport 'PLAIN' nil nil nil {} 
                      (fn [it] (condp instance? it
                                 NameCallback (.setName it username)
                                 PasswordCallback (.setPassword (char-array it) ))) 
                      (new TSocket username password))
          connection (->> transport (new TBinaryProtocol) (new TCLIService$Client))
          session (->> (TProtocolVersion/findByValue api-version) (new TOpenSessionReq) (.OpenSession connection) .getSsessionHandle)]
      {:transport transport :connection transport :session transport 
       :args [hostname port username password api-version]})))

(defn open-if [client]
  (if-not (.open (:transport client)) (apply open (:args client)) client)  )

(defn execute-statement [origin-client sql]
  (let [client (open-if origin-client)]
     (-> (.ExecuteStatement (:connection client) (new TExecuteStatementReq (:session client) sql))
         .getOperationHandle))  )

(defn read-sql-columns [origin-client sql]
  (let [client (open-if origin-client)
        op-handle (execute-statement client sql)
        column->edn (fn [column-obj] {:columnName (.getColumnName column-obj) 
                                      :type (-> column-obj .getTypeDesc .getTypes first)}
                                      :comment (.getComment column-obj))
        result (->> op-handle (new TGetResultSetMetadataReq) (.GetResultSetMetadata (:connection client)) .getSchema .getColumns column->edn)
        _  (->> op-handle (new TCloseOperationReq) (.CloseOperation (:connection client) )) ]
    result)  )

(comment
  (read-sql-columns (open "192.168.1.3") "select * from table")
  )
