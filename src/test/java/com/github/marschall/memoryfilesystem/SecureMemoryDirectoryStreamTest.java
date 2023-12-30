package com.github.marschall.memoryfilesystem;

import static com.github.marschall.memoryfilesystem.FileExistsMatcher.exists;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.ClosedDirectoryStreamException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SecureDirectoryStream;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class SecureMemoryDirectoryStreamTest {

  @RegisterExtension
  final FileSystemExtension extension = new FileSystemExtension();

  @Test
  void deleteFileRelative() throws IOException {
    FileSystem fileSystem = this.extension.getFileSystem();

    Path originalFolder = fileSystem.getPath("original-folder");
    Files.createDirectory(originalFolder);
    Files.createFile(originalFolder.resolve("child"));
    Files.createFile(fileSystem.getPath("child"));

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(originalFolder)) {
      assumeTrue(directoryStream instanceof SecureDirectoryStream);
      SecureDirectoryStream<Path> secure = (SecureDirectoryStream<Path>) directoryStream;

      secure.deleteFile(fileSystem.getPath("child"));

      assertThat(fileSystem.getPath("child"), exists());
      assertThat(originalFolder.resolve("child"), not(exists()));
    }

  }

  @Test
  void deleteFileAbsolute() throws IOException {
    FileSystem fileSystem = this.extension.getFileSystem();

    Path originalFolder = fileSystem.getPath("original-folder");
    Files.createDirectory(originalFolder);
    Files.createFile(originalFolder.resolve("child"));
    Files.createFile(fileSystem.getPath("child"));

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(originalFolder)) {
      assumeTrue(directoryStream instanceof SecureDirectoryStream);
      SecureDirectoryStream<Path> secure = (SecureDirectoryStream<Path>) directoryStream;

      secure.deleteFile(fileSystem.getPath("child").toAbsolutePath());

      assertThat(fileSystem.getPath("child"), not(exists()));
      assertThat(originalFolder.resolve("child"), exists());
    }

  }

  @Test
  void deleteFileClosed() throws IOException {
    FileSystem fileSystem = this.extension.getFileSystem();

    Path originalFolder = fileSystem.getPath("original-folder");
    Files.createDirectory(originalFolder);

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(originalFolder)) {
      assumeTrue(directoryStream instanceof SecureDirectoryStream);
      SecureDirectoryStream<Path> secure = (SecureDirectoryStream<Path>) directoryStream;

      assertThrows(ClosedDirectoryStreamException.class,
              () -> secure.deleteFile(fileSystem.getPath("/")),
              "closed secure directory stream should throw");

    }

  }

  @Test
  void deleteDirectoryRelative() throws IOException {
    FileSystem fileSystem = this.extension.getFileSystem();

    Path originalFolder = fileSystem.getPath("original-folder");
    Files.createDirectory(originalFolder);
    Files.createDirectory(originalFolder.resolve("child"));
    Files.createDirectory(fileSystem.getPath("child"));

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(originalFolder)) {
      assumeTrue(directoryStream instanceof SecureDirectoryStream);
      SecureDirectoryStream<Path> secure = (SecureDirectoryStream<Path>) directoryStream;

      secure.deleteDirectory(fileSystem.getPath("child"));

      assertThat(fileSystem.getPath("child"), exists());
      assertThat(originalFolder.resolve("child"), not(exists()));
    }

  }

  @Test
  void deleteDirectoryAbsolute() throws IOException {
    FileSystem fileSystem = this.extension.getFileSystem();

    Path originalFolder = fileSystem.getPath("original-folder");
    Files.createDirectory(originalFolder);
    Files.createDirectory(originalFolder.resolve("child"));
    Files.createDirectory(fileSystem.getPath("child"));

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(originalFolder)) {
      assumeTrue(directoryStream instanceof SecureDirectoryStream);
      SecureDirectoryStream<Path> secure = (SecureDirectoryStream<Path>) directoryStream;

      secure.deleteDirectory(fileSystem.getPath("child").toAbsolutePath());

      assertThat(fileSystem.getPath("child"), not(exists()));
      assertThat(originalFolder.resolve("child"), exists());
    }

  }

  @Test
  void deleteDirectoryClosed() throws IOException {
    FileSystem fileSystem = this.extension.getFileSystem();

    Path originalFolder = fileSystem.getPath("original-folder");
    Files.createDirectory(originalFolder);

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(originalFolder)) {
      assumeTrue(directoryStream instanceof SecureDirectoryStream);
      SecureDirectoryStream<Path> secure = (SecureDirectoryStream<Path>) directoryStream;

      assertThrows(ClosedDirectoryStreamException.class,
              () -> secure.deleteDirectory(fileSystem.getPath("/")),
              "closed secure directory stream should throw");

    }

  }

  @Test
  void newByteChannelRelative() throws IOException {
    FileSystem fileSystem = this.extension.getFileSystem();

    Path originalFolder = fileSystem.getPath("original-folder");
    Files.createDirectory(originalFolder);

    Path relativePath = originalFolder.resolve("child");
    Files.createFile(relativePath);
    FileUtility.setContents(relativePath, "relative");

    Path absolutePath = fileSystem.getPath("child").toAbsolutePath();
    Files.createFile(absolutePath);
    FileUtility.setContents(absolutePath, "absolute");

    try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(originalFolder)) {
      assumeTrue(directoryStream instanceof SecureDirectoryStream);
      SecureDirectoryStream<Path> secure = (SecureDirectoryStream<Path>) directoryStream;

      try (SeekableByteChannel channel = secure.newByteChannel(absolutePath, Collections.singleton(StandardOpenOption.READ))) {

      }

    }

  }

}
