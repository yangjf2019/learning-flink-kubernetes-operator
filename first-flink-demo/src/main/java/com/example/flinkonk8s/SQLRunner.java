package com.example.flinkonk8s;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.TableResult;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
/**
 * @Author Created by Jeff Yang on 11/8/23 13:53.
 * @Update date:
 * @Project: first-flink-demo
 * @Package: com.example.flinkonk8s
 * @Describe: 输入 sql statement，然后直接执行
 */
public class SQLRunner {
    private static final Logger LOG = LoggerFactory.getLogger(SQLRunner.class);
    /** a statement should end with `;` */
    private static final String STATEMENT_DELIMITER = ";";
    private static final String LINE_DELIMITER = "\n";
    private static final String COMMENT_PATTERN = "(--.*)|(((\\/\\*)+?[\\w\\W]+?(\\*\\/)+))";

    public static void main(String[] args) throws Exception {
        String script = "";
        if (args.length == 1) {
            script = args[0];
        }
        // 解析输入的 sql statement 字符串
        List<String> statements = parseStatements(script);

        // 建立 stream env
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment().setParallelism(1);
        // 建立 table env
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        TableResult tableResult = null;
        for (String statement : statements) {
            LOG.info("Executing:\n{}", statement);
            tableResult = tableEnv.executeSql(statement);
        }
        // executeSql 是一个异步的接口，若在 idea 里面跑，直接就结束了，需手动拿到那个 executeSql 的返回的 TableResult
        assert tableResult != null;
        tableResult
                .getJobClient()
                .get()
                .getJobExecutionResult()
                .get();
    }

    /**
     * 解析 sql script
     * @param script 字符串
     * @return 解析后的 sql
     * 参考语法：<a href="https://nightlies.apache.org/flink/flink-docs-release-1.15/docs/dev/table/sqlclient/#execute-a-set-of-sql-statements">flink sql 语法</a>
     */
    public static List<String> parseStatements(String script) {
        // 处理注释
        String formatted = formatSqlFile(script).replaceAll(COMMENT_PATTERN, "");
        List<String> statements = new ArrayList<>();
        StringBuilder current = null;
        boolean statementSet = false;
        // 根据换行处理输入的 sql 内容
        for (String line : formatted.split(LINE_DELIMITER)) {
            String trimmed = line.trim();
            if (trimmed.length() < 1) {
                continue;
            }
            if (current == null) {
                current = new StringBuilder();
            }
            if (trimmed.startsWith("EXECUTE STATEMENT SET") || trimmed.startsWith("BEGIN")) {
                statementSet = true;
            }
            current.append(trimmed);
            current.append(LINE_DELIMITER);
            if (trimmed.endsWith(STATEMENT_DELIMITER)) {
                if (!statementSet || trimmed.equals("END;")) {
                    // SQL 语句不能以分号结尾
                    statements.add(current.toString().replace(";", ""));
                    current = null;
                    statementSet = false;
                }
            }
        }
        return statements;
    }

    /**
     * 格式化
     * @param content sql 内容
     * @return 格式化后的 sql
     */
    private static String formatSqlFile(String content) {
        String trimmed = content.trim();
        StringBuilder formatted = new StringBuilder();
        formatted.append(trimmed);
        if (!trimmed.endsWith(STATEMENT_DELIMITER)) {
            formatted.append(STATEMENT_DELIMITER);
        }
        formatted.append(LINE_DELIMITER);
        return formatted.toString();
    }
}
