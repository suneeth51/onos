package org.onlab.onos.store.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import net.kuujo.copycat.internal.log.OperationEntry;
import net.kuujo.copycat.log.Entry;
import net.kuujo.copycat.log.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.onlab.onos.store.serializers.StoreSerializer;

import com.google.common.testing.EqualsTester;

/**
 * Test the MapDBLog implementation.
 */
public class MapDBLogTest {

    private static final String DB_FILE_NAME = "mapdbTest";
    private static final StoreSerializer SERIALIZER = ClusterMessagingProtocol.SERIALIZER;
    private static final Entry TEST_ENTRY1 = new OperationEntry(1, "test1");
    private static final Entry TEST_ENTRY2 = new OperationEntry(2, "test12");
    private static final Entry TEST_ENTRY3 = new OperationEntry(3, "test123");
    private static final Entry TEST_ENTRY4 = new OperationEntry(4, "test1234");

    private static final Entry TEST_SNAPSHOT_ENTRY = new OperationEntry(5, "snapshot");

    private static final long TEST_ENTRY1_SIZE = SERIALIZER.encode(TEST_ENTRY1).length;
    private static final long TEST_ENTRY2_SIZE = SERIALIZER.encode(TEST_ENTRY2).length;
    private static final long TEST_ENTRY3_SIZE = SERIALIZER.encode(TEST_ENTRY3).length;
    private static final long TEST_ENTRY4_SIZE = SERIALIZER.encode(TEST_ENTRY4).length;

    private static final long TEST_SNAPSHOT_ENTRY_SIZE = SERIALIZER.encode(TEST_SNAPSHOT_ENTRY).length;

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(new File(DB_FILE_NAME).toPath());
        Files.deleteIfExists(new File(DB_FILE_NAME + ".t").toPath());
        Files.deleteIfExists(new File(DB_FILE_NAME + ".p").toPath());
    }

    @Test(expected = IllegalStateException.class)
    public void testAssertOpen() {
        Log log = new MapDBLog(new File(DB_FILE_NAME), SERIALIZER);
        log.size();
    }

    @Test
    public void testAppendEntry() throws IOException {
        Log log = new MapDBLog(new File(DB_FILE_NAME), SERIALIZER);
        log.open();
        log.appendEntry(TEST_ENTRY1);
        OperationEntry first = log.firstEntry();
        OperationEntry last = log.lastEntry();
        new EqualsTester()
            .addEqualityGroup(first, last, TEST_ENTRY1)
            .testEquals();
        Assert.assertEquals(TEST_ENTRY1_SIZE, log.size());
        Assert.assertEquals(1, log.firstIndex());
        Assert.assertEquals(1, log.lastIndex());
    }

    @Test
    public void testAppendEntries() throws IOException {
        Log log = new MapDBLog(new File(DB_FILE_NAME), SERIALIZER);
        log.open();
        log.appendEntries(TEST_ENTRY1, TEST_ENTRY2, TEST_ENTRY3);
        OperationEntry first = log.firstEntry();
        OperationEntry last = log.lastEntry();
        new EqualsTester()
            .addEqualityGroup(first, TEST_ENTRY1)
            .addEqualityGroup(last, TEST_ENTRY3)
            .testEquals();
        Assert.assertEquals(TEST_ENTRY1_SIZE + TEST_ENTRY2_SIZE, TEST_ENTRY3_SIZE, log.size());
        Assert.assertEquals(1, log.firstIndex());
        Assert.assertEquals(3, log.lastIndex());
        Assert.assertTrue(log.containsEntry(1));
        Assert.assertTrue(log.containsEntry(2));
    }

    @Test
    public void testDelete() throws IOException {
        Log log = new MapDBLog(new File(DB_FILE_NAME), SERIALIZER);
        log.open();
        log.appendEntries(TEST_ENTRY1, TEST_ENTRY2);
        log.delete();
        Assert.assertEquals(0, log.size());
        Assert.assertTrue(log.isEmpty());
        Assert.assertEquals(0, log.firstIndex());
        Assert.assertNull(log.firstEntry());
        Assert.assertEquals(0, log.lastIndex());
        Assert.assertNull(log.lastEntry());
    }

    @Test
    public void testGetEntries() throws IOException {
        Log log = new MapDBLog(new File(DB_FILE_NAME), SERIALIZER);
        log.open();
        log.appendEntries(TEST_ENTRY1, TEST_ENTRY2, TEST_ENTRY3, TEST_ENTRY4);
        Assert.assertEquals(
                TEST_ENTRY1_SIZE +
                TEST_ENTRY2_SIZE +
                TEST_ENTRY3_SIZE +
                TEST_ENTRY4_SIZE, log.size());

        List<Entry> entries = log.getEntries(2, 3);
        new EqualsTester()
            .addEqualityGroup(log.getEntry(4), TEST_ENTRY4)
            .addEqualityGroup(entries.get(0), TEST_ENTRY2)
            .addEqualityGroup(entries.get(1), TEST_ENTRY3)
            .testEquals();
    }

    @Test
    public void testRemoveAfter() throws IOException {
        Log log = new MapDBLog(new File(DB_FILE_NAME), SERIALIZER);
        log.open();
        log.appendEntries(TEST_ENTRY1, TEST_ENTRY2, TEST_ENTRY3, TEST_ENTRY4);
        log.removeAfter(1);
        Assert.assertEquals(TEST_ENTRY1_SIZE, log.size());
        new EqualsTester()
            .addEqualityGroup(log.firstEntry(), log.lastEntry(), TEST_ENTRY1)
            .testEquals();
    }

    @Test
    public void testAddAfterRemove() throws IOException {
        Log log = new MapDBLog(new File(DB_FILE_NAME), SERIALIZER);
        log.open();
        log.appendEntries(TEST_ENTRY1, TEST_ENTRY2, TEST_ENTRY3, TEST_ENTRY4);
        log.removeAfter(1);
        log.appendEntry(TEST_ENTRY4);
        Assert.assertEquals(TEST_ENTRY1_SIZE + TEST_ENTRY4_SIZE, log.size());
        new EqualsTester()
            .addEqualityGroup(log.firstEntry(), TEST_ENTRY1)
            .addEqualityGroup(log.lastEntry(), TEST_ENTRY4)
            .addEqualityGroup(log.size(), TEST_ENTRY1_SIZE + TEST_ENTRY4_SIZE)
            .testEquals();
    }

    @Test
    public void testClose() throws IOException {
        Log log = new MapDBLog(new File(DB_FILE_NAME), SERIALIZER);
        Assert.assertFalse(log.isOpen());
        log.open();
        Assert.assertTrue(log.isOpen());
        log.close();
        Assert.assertFalse(log.isOpen());
    }

    @Test
    public void testReopen() throws IOException {
        Log log = new MapDBLog(new File(DB_FILE_NAME), SERIALIZER);
        log.open();
        log.appendEntries(TEST_ENTRY1, TEST_ENTRY2, TEST_ENTRY3, TEST_ENTRY4);
        log.close();
        log.open();

        new EqualsTester()
            .addEqualityGroup(log.firstEntry(), TEST_ENTRY1)
            .addEqualityGroup(log.getEntry(2), TEST_ENTRY2)
            .addEqualityGroup(log.lastEntry(), TEST_ENTRY4)
            .addEqualityGroup(log.size(),
                    TEST_ENTRY1_SIZE +
                    TEST_ENTRY2_SIZE +
                    TEST_ENTRY3_SIZE +
                    TEST_ENTRY4_SIZE)
            .testEquals();
    }

    @Test
    public void testCompact() throws IOException {
        Log log = new MapDBLog(new File(DB_FILE_NAME), SERIALIZER);
        log.open();
        log.appendEntries(TEST_ENTRY1, TEST_ENTRY2, TEST_ENTRY3, TEST_ENTRY4);
        log.compact(3, TEST_SNAPSHOT_ENTRY);
        new EqualsTester()
        .addEqualityGroup(log.firstEntry(), TEST_SNAPSHOT_ENTRY)
        .addEqualityGroup(log.lastEntry(), TEST_ENTRY4)
        .addEqualityGroup(log.size(),
                TEST_SNAPSHOT_ENTRY_SIZE +
                TEST_ENTRY4_SIZE)
        .testEquals();
    }
}