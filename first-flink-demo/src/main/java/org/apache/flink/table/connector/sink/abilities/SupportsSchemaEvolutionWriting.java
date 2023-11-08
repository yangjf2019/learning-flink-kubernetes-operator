package org.apache.flink.table.connector.sink.abilities;

/**
 * @Author Created by Jeff Yang on 1/9/23 16:07.
 * @Update date:
 * @Project: flink-on-k8s
 * @Package: org.apache.flink.table.connector.sink.abilities
 * @Describe: 必须添加该接口名称，可没方法
 * 参考：https://blog.csdn.net/high2011/article/details/128625289
 */
import org.apache.flink.annotation.PublicEvolving;

@PublicEvolving
public interface SupportsSchemaEvolutionWriting {
    void applySchemaEvolutionWriting(boolean var1);
}
