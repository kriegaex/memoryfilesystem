package com.github.marschall.memoryfilesystem;

import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;


class PosixMemoryFileSystemTest {

  @RegisterExtension
  final PosixFileSystemExtension extension = new PosixFileSystemExtension();

  @Test
  void defaultAttributes() throws IOException {
    FileSystem fileSystem = this.extension.getFileSystem();
    Path file = fileSystem.getPath("file.txt");

    Files.createFile(file);

    PosixFileAttributeView sourcePosixFileAttributeView = Files.getFileAttributeView(file, PosixFileAttributeView.class);
    PosixFileAttributes sourcePosixAttributes = sourcePosixFileAttributeView.readAttributes();
    assertNotNull(sourcePosixAttributes.permissions(), "permissions");
    assertNotNull(sourcePosixAttributes.owner(), "owner");
    assertNotNull(sourcePosixAttributes.group(), "group");
  }

  @Test
  void getOwner() throws IOException {
    FileSystem fileSystem = this.extension.getFileSystem();
    UserPrincipal owner = Files.getOwner(fileSystem.getPath("/"));
    assertNotNull(owner);
  }

  @Test
  void supportedFileAttributeViews() {
    FileSystem fileSystem = this.extension.getFileSystem();
    Set<String> actual = fileSystem.supportedFileAttributeViews();
    Set<String> expected = new HashSet<>(Arrays.asList("basic", "owner", "posix"));
    assertEquals(expected, actual);
  }

  @Test
  void copyAttributes() throws IOException {
    FileSystem fileSystem = this.extension.getFileSystem();
    Path source = fileSystem.getPath("source.txt");
    Path target = fileSystem.getPath("target.txt");

    Files.createFile(source);

    PosixFileAttributeView sourcePosixFileAttributeView = Files.getFileAttributeView(source, PosixFileAttributeView.class);

    EnumSet<PosixFilePermission> permissions = EnumSet.of(OWNER_READ, OWNER_WRITE);
    sourcePosixFileAttributeView.setPermissions(permissions);

    Files.copy(source, target, StandardCopyOption.COPY_ATTRIBUTES);

    PosixFileAttributeView targetPosixFileAttributeView = Files.getFileAttributeView(target, PosixFileAttributeView.class);
    PosixFileAttributes targetPosixAttributes = targetPosixFileAttributeView.readAttributes();
    assertEquals(permissions, targetPosixAttributes.permissions());
    assertNotSame(permissions, targetPosixAttributes.permissions());
  }

  @Test
  void dontCopyAttributes() throws IOException {
    FileSystem fileSystem = this.extension.getFileSystem();
    Path source = fileSystem.getPath("source.txt");
    Path target = fileSystem.getPath("target.txt");

    Files.createFile(source);

    PosixFileAttributeView sourcePosixFileAttributeView = Files.getFileAttributeView(source, PosixFileAttributeView.class);

    EnumSet<PosixFilePermission> permissions = EnumSet.of(OWNER_READ, OWNER_WRITE);
    sourcePosixFileAttributeView.setPermissions(permissions);

    Files.copy(source, target);

    PosixFileAttributeView targetPosixFileAttributeView = Files.getFileAttributeView(target, PosixFileAttributeView.class);
    PosixFileAttributes targetPosixAttributes = targetPosixFileAttributeView.readAttributes();
    assertNotEquals(permissions, targetPosixAttributes.permissions());
  }

  // https://bugs.openjdk.java.net/browse/JDK-8066915
  @Test
  void jdk8066915() throws IOException {
    FileSystem fileSystem = this.extension.getFileSystem();
    Path directory = fileSystem.getPath("directory");
    Files.createDirectory(directory);

    try (ByteChannel channel = Files.newByteChannel(directory)) {
      fail("should not be able to create channel on directory");
    } catch (FileSystemException e) {
      // should reach here
      assertEquals(directory.toAbsolutePath().toString(), e.getFile(), "file");
    }

    try (ByteChannel channel = Files.newByteChannel(directory, READ)) {
      fail("should not be able to create channel on directory");

    } catch (FileSystemException e) {
      // should reach here
      assertEquals(directory.toAbsolutePath().toString(), e.getFile(), "file");
    }

    try (ByteChannel channel = Files.newByteChannel(directory, WRITE)) {
      fail("should not be able to create channel on directory");
    } catch (FileSystemException e) {
      // should reach here
      assertEquals(directory.toAbsolutePath().toString(), e.getFile(), "file");
    }
  }

  @Test
  void noTruncation() throws IOException {
    FileSystem fileSystem = this.extension.getFileSystem();
    Instant mtime = Instant.parse("2019-02-27T12:37:03.123456789Z");
    Instant atime = Instant.parse("2019-02-27T12:37:03.223456789Z");
    Instant ctime = Instant.parse("2019-02-27T12:37:03.323456789Z");

    Path file = Files.createFile(fileSystem.getPath("C:\\file.txt"));
    BasicFileAttributeView view = Files.getFileAttributeView(file, BasicFileAttributeView.class);
    view.setTimes(FileTime.from(mtime), FileTime.from(atime), FileTime.from(ctime));

    BasicFileAttributes attributes = view.readAttributes();

    assertEquals(mtime, attributes.lastModifiedTime().toInstant());
    assertEquals(atime, attributes.lastAccessTime().toInstant());
    assertEquals(ctime, attributes.creationTime().toInstant());
  }

}
