/*
 * Copyright 2019 HiveMQ and the HiveMQ Community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hivemq.backup.mosquitto.extension;

import org.junit.jupiter.api.extension.*;

import java.io.File;
import java.io.IOException;

/**
 * @author Lukas Brand
 * @since 1.0.0
 */
//This class is a reimplementation of the JUnit 4 TemporaryFolder Rule as JUnit5 Extension
public class TemporaryFolderExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        create();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        delete();
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(TemporaryFolderExtension.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return new TemporaryFolderExtension();
    }


    private final File parentFolder;
    private File folder;

    public TemporaryFolderExtension() {
        this((File)null);
    }

    public TemporaryFolderExtension(File parentFolder) {
        this.parentFolder = parentFolder;
    }

    public TemporaryFolderExtension(String parentFolder) {
        this.parentFolder = new File(parentFolder);
    }


    // testing purposes only

    /**
     * for testing purposes only. Do not use.
     */
    public void create() throws IOException {
        folder = createTemporaryFolderIn(parentFolder);
    }

    /**
     * Returns a new fresh file with the given name under the temporary folder.
     */
    public File newFile(String fileName) throws IOException {
        File file = new File(getRoot(), fileName);
        if (!file.createNewFile()) {
            throw new IOException(
                    "a file with the name \'" + fileName + "\' already exists in the test folder");
        }
        return file;
    }

    /**
     * Returns a new fresh file with a random name under the temporary folder.
     */
    public File newFile() throws IOException {
        return File.createTempFile("junit", null, getRoot());
    }

    /**
     * Returns a new fresh folder with the given name under the temporary
     * folder.
     */
    public File newFolder(String folder) throws IOException {
        return newFolder(new String[]{folder});
    }

    /**
     * Returns a new fresh folder with the given name(s) under the temporary
     * folder.
     */
    public File newFolder(String... folderNames) throws IOException {
        File file = getRoot();
        for (int i = 0; i < folderNames.length; i++) {
            String folderName = folderNames[i];
            validateFolderName(folderName);
            file = new File(file, folderName);
            if (!file.mkdir() && isLastElementInArray(i, folderNames)) {
                throw new IOException(
                        "a folder with the name \'" + folderName + "\' already exists");
            }
        }
        return file;
    }

    /**
     * Validates if multiple path components were used while creating a folder.
     *
     * @param folderName
     *            Name of the folder being created
     */
    private void validateFolderName(String folderName) throws IOException {
        File tempFile = new File(folderName);
        if (tempFile.getParent() != null) {
            String errorMsg = "Folder name cannot consist of multiple path components separated by a file separator."
                    + " Please use newFolder('MyParentFolder','MyFolder') to create hierarchies of folders";
            throw new IOException(errorMsg);
        }
    }

    private boolean isLastElementInArray(int index, String[] array) {
        return index == array.length - 1;
    }

    /**
     * Returns a new fresh folder with a random name under the temporary folder.
     */
    public File newFolder() throws IOException {
        return createTemporaryFolderIn(getRoot());
    }

    private File createTemporaryFolderIn(File parentFolder) throws IOException {
        File createdFolder = File.createTempFile("junit", "", parentFolder);
        createdFolder.delete();
        createdFolder.mkdir();
        return createdFolder;
    }

    /**
     * @return the location of this temporary folder.
     */
    public File getRoot() {
        if (folder == null) {
            throw new IllegalStateException(
                    "the temporary folder has not yet been created");
        }
        return folder;
    }

    /**
     * Delete all files and folders under the temporary folder. Usually not
     * called directly, since it is automatically applied by the
     */
    public void delete() {
        if (folder != null) {
            recursiveDelete(folder);
        }
    }

    private void recursiveDelete(File file) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File each : files) {
                recursiveDelete(each);
            }
        }
        file.delete();
    }



}
