// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: ForumContract.proto

// Protobuf Java Version: 3.25.5
package forum;

public interface ExistingTopicsOrBuilder extends
    // @@protoc_insertion_point(interface_extends:forum.ExistingTopics)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * list of topic names
   * </pre>
   *
   * <code>repeated string topicName = 1;</code>
   * @return A list containing the topicName.
   */
  java.util.List<java.lang.String>
      getTopicNameList();
  /**
   * <pre>
   * list of topic names
   * </pre>
   *
   * <code>repeated string topicName = 1;</code>
   * @return The count of topicName.
   */
  int getTopicNameCount();
  /**
   * <pre>
   * list of topic names
   * </pre>
   *
   * <code>repeated string topicName = 1;</code>
   * @param index The index of the element to return.
   * @return The topicName at the given index.
   */
  java.lang.String getTopicName(int index);
  /**
   * <pre>
   * list of topic names
   * </pre>
   *
   * <code>repeated string topicName = 1;</code>
   * @param index The index of the value to return.
   * @return The bytes of the topicName at the given index.
   */
  com.google.protobuf.ByteString
      getTopicNameBytes(int index);
}
