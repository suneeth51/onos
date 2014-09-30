package org.onlab.onos.net.trivial.impl;

import static org.junit.Assert.*;
import static org.onlab.onos.net.DeviceId.deviceId;
import static org.onlab.onos.net.Link.Type.*;
import static org.onlab.onos.net.link.LinkEvent.Type.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.onlab.onos.net.ConnectPoint;
import org.onlab.onos.net.DeviceId;
import org.onlab.onos.net.Link;
import org.onlab.onos.net.LinkKey;
import org.onlab.onos.net.PortNumber;
import org.onlab.onos.net.Link.Type;
import org.onlab.onos.net.link.DefaultLinkDescription;
import org.onlab.onos.net.link.LinkEvent;
import org.onlab.onos.net.link.LinkStore;
import org.onlab.onos.net.link.LinkStoreDelegate;
import org.onlab.onos.net.provider.ProviderId;

import com.google.common.collect.Iterables;

/**
 * Test of the simple LinkStore implementation.
 */
public class SimpleLinkStoreTest {

    private static final ProviderId PID = new ProviderId("of", "foo");
    private static final DeviceId DID1 = deviceId("of:foo");
    private static final DeviceId DID2 = deviceId("of:bar");

    private static final PortNumber P1 = PortNumber.portNumber(1);
    private static final PortNumber P2 = PortNumber.portNumber(2);
    private static final PortNumber P3 = PortNumber.portNumber(3);


    private SimpleLinkStore simpleLinkStore;
    private LinkStore linkStore;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        simpleLinkStore = new SimpleLinkStore();
        simpleLinkStore.activate();
        linkStore = simpleLinkStore;
    }

    @After
    public void tearDown() throws Exception {
        simpleLinkStore.deactivate();
    }

    private void putLink(DeviceId srcId, PortNumber srcNum,
                         DeviceId dstId, PortNumber dstNum, Type type) {
        ConnectPoint src = new ConnectPoint(srcId, srcNum);
        ConnectPoint dst = new ConnectPoint(dstId, dstNum);
        linkStore.createOrUpdateLink(PID, new DefaultLinkDescription(src, dst, type));
    }

    private void putLink(LinkKey key, Type type) {
        putLink(key.src().deviceId(), key.src().port(),
                key.dst().deviceId(), key.dst().port(),
                type);
    }

    private static void assertLink(DeviceId srcId, PortNumber srcNum,
                            DeviceId dstId, PortNumber dstNum, Type type,
                            Link link) {
        assertEquals(srcId, link.src().deviceId());
        assertEquals(srcNum, link.src().port());
        assertEquals(dstId, link.dst().deviceId());
        assertEquals(dstNum, link.dst().port());
        assertEquals(type, link.type());
    }

    private static void assertLink(LinkKey key, Type type, Link link) {
        assertLink(key.src().deviceId(), key.src().port(),
                   key.dst().deviceId(), key.dst().port(),
                   type, link);
    }

    @Test
    public final void testGetLinkCount() {
        assertEquals("initialy empty", 0, linkStore.getLinkCount());

        putLink(DID1, P1, DID2, P2, DIRECT);
        putLink(DID2, P2, DID1, P1, DIRECT);
        putLink(DID1, P1, DID2, P2, DIRECT);

        assertEquals("expecting 2 unique link", 2, linkStore.getLinkCount());
    }

    @Test
    public final void testGetLinks() {
        assertEquals("initialy empty", 0,
                Iterables.size(linkStore.getLinks()));

        LinkKey linkId1 = new LinkKey(new ConnectPoint(DID1, P1), new ConnectPoint(DID2, P2));
        LinkKey linkId2 = new LinkKey(new ConnectPoint(DID2, P2), new ConnectPoint(DID1, P1));

        putLink(linkId1, DIRECT);
        putLink(linkId2, DIRECT);
        putLink(linkId1, DIRECT);

        assertEquals("expecting 2 unique link", 2,
                Iterables.size(linkStore.getLinks()));

        Map<LinkKey, Link> links = new HashMap<>();
        for (Link link : linkStore.getLinks()) {
            links.put(new LinkKey(link.src(), link.dst()), link);
        }

        assertLink(linkId1, DIRECT, links.get(linkId1));
        assertLink(linkId2, DIRECT, links.get(linkId2));
    }

    @Test
    public final void testGetDeviceEgressLinks() {
        LinkKey linkId1 = new LinkKey(new ConnectPoint(DID1, P1), new ConnectPoint(DID2, P2));
        LinkKey linkId2 = new LinkKey(new ConnectPoint(DID2, P2), new ConnectPoint(DID1, P1));
        LinkKey linkId3 = new LinkKey(new ConnectPoint(DID1, P2), new ConnectPoint(DID2, P3));

        putLink(linkId1, DIRECT);
        putLink(linkId2, DIRECT);
        putLink(linkId3, DIRECT);

        // DID1,P1 => DID2,P2
        // DID2,P2 => DID1,P1
        // DID1,P2 => DID2,P3

        Set<Link> links1 = linkStore.getDeviceEgressLinks(DID1);
        assertEquals(2, links1.size());
        // check

        Set<Link> links2 = linkStore.getDeviceEgressLinks(DID2);
        assertEquals(1, links2.size());
        assertLink(linkId2, DIRECT, links2.iterator().next());
    }

    @Test
    public final void testGetDeviceIngressLinks() {
        LinkKey linkId1 = new LinkKey(new ConnectPoint(DID1, P1), new ConnectPoint(DID2, P2));
        LinkKey linkId2 = new LinkKey(new ConnectPoint(DID2, P2), new ConnectPoint(DID1, P1));
        LinkKey linkId3 = new LinkKey(new ConnectPoint(DID1, P2), new ConnectPoint(DID2, P3));

        putLink(linkId1, DIRECT);
        putLink(linkId2, DIRECT);
        putLink(linkId3, DIRECT);

        // DID1,P1 => DID2,P2
        // DID2,P2 => DID1,P1
        // DID1,P2 => DID2,P3

        Set<Link> links1 = linkStore.getDeviceIngressLinks(DID2);
        assertEquals(2, links1.size());
        // check

        Set<Link> links2 = linkStore.getDeviceIngressLinks(DID1);
        assertEquals(1, links2.size());
        assertLink(linkId2, DIRECT, links2.iterator().next());
    }

    @Test
    public final void testGetLink() {
        ConnectPoint src = new ConnectPoint(DID1, P1);
        ConnectPoint dst = new ConnectPoint(DID2, P2);
        LinkKey linkId1 = new LinkKey(src, dst);

        putLink(linkId1, DIRECT);

        Link link = linkStore.getLink(src, dst);
        assertLink(linkId1, DIRECT, link);

        assertNull("There shouldn't be reverese link",
                linkStore.getLink(dst, src));
    }

    @Test
    public final void testGetEgressLinks() {
        final ConnectPoint d1P1 = new ConnectPoint(DID1, P1);
        final ConnectPoint d2P2 = new ConnectPoint(DID2, P2);
        LinkKey linkId1 = new LinkKey(d1P1, d2P2);
        LinkKey linkId2 = new LinkKey(d2P2, d1P1);
        LinkKey linkId3 = new LinkKey(new ConnectPoint(DID1, P2), new ConnectPoint(DID2, P3));

        putLink(linkId1, DIRECT);
        putLink(linkId2, DIRECT);
        putLink(linkId3, DIRECT);

        // DID1,P1 => DID2,P2
        // DID2,P2 => DID1,P1
        // DID1,P2 => DID2,P3

        Set<Link> links1 = linkStore.getEgressLinks(d1P1);
        assertEquals(1, links1.size());
        assertLink(linkId1, DIRECT, links1.iterator().next());

        Set<Link> links2 = linkStore.getEgressLinks(d2P2);
        assertEquals(1, links2.size());
        assertLink(linkId2, DIRECT, links2.iterator().next());
    }

    @Test
    public final void testGetIngressLinks() {
        final ConnectPoint d1P1 = new ConnectPoint(DID1, P1);
        final ConnectPoint d2P2 = new ConnectPoint(DID2, P2);
        LinkKey linkId1 = new LinkKey(d1P1, d2P2);
        LinkKey linkId2 = new LinkKey(d2P2, d1P1);
        LinkKey linkId3 = new LinkKey(new ConnectPoint(DID1, P2), new ConnectPoint(DID2, P3));

        putLink(linkId1, DIRECT);
        putLink(linkId2, DIRECT);
        putLink(linkId3, DIRECT);

        // DID1,P1 => DID2,P2
        // DID2,P2 => DID1,P1
        // DID1,P2 => DID2,P3

        Set<Link> links1 = linkStore.getIngressLinks(d2P2);
        assertEquals(1, links1.size());
        assertLink(linkId1, DIRECT, links1.iterator().next());

        Set<Link> links2 = linkStore.getIngressLinks(d1P1);
        assertEquals(1, links2.size());
        assertLink(linkId2, DIRECT, links2.iterator().next());
    }

    @Test
    public final void testCreateOrUpdateLink() {
        ConnectPoint src = new ConnectPoint(DID1, P1);
        ConnectPoint dst = new ConnectPoint(DID2, P2);

        // add link
        LinkEvent event = linkStore.createOrUpdateLink(PID,
                    new DefaultLinkDescription(src, dst, INDIRECT));

        assertLink(DID1, P1, DID2, P2, INDIRECT, event.subject());
        assertEquals(LINK_ADDED, event.type());

        // update link type
        LinkEvent event2 = linkStore.createOrUpdateLink(PID,
                new DefaultLinkDescription(src, dst, DIRECT));

        assertLink(DID1, P1, DID2, P2, DIRECT, event2.subject());
        assertEquals(LINK_UPDATED, event2.type());

        // no change
        LinkEvent event3 = linkStore.createOrUpdateLink(PID,
                new DefaultLinkDescription(src, dst, DIRECT));

        assertNull("No change event expected", event3);
    }

    @Test
    public final void testRemoveLink() {
        final ConnectPoint d1P1 = new ConnectPoint(DID1, P1);
        final ConnectPoint d2P2 = new ConnectPoint(DID2, P2);
        LinkKey linkId1 = new LinkKey(d1P1, d2P2);
        LinkKey linkId2 = new LinkKey(d2P2, d1P1);

        putLink(linkId1, DIRECT);
        putLink(linkId2, DIRECT);

        // DID1,P1 => DID2,P2
        // DID2,P2 => DID1,P1
        // DID1,P2 => DID2,P3

        LinkEvent event = linkStore.removeLink(d1P1, d2P2);
        assertEquals(LINK_REMOVED, event.type());
        LinkEvent event2 = linkStore.removeLink(d1P1, d2P2);
        assertNull(event2);

        assertLink(linkId2, DIRECT, linkStore.getLink(d2P2, d1P1));
    }

    // If Delegates should be called only on remote events,
    // then Simple* should never call them, thus not test required.
    @Ignore("Ignore until Delegate spec. is clear.")
    @Test
    public final void testEvents() throws InterruptedException {

        final ConnectPoint d1P1 = new ConnectPoint(DID1, P1);
        final ConnectPoint d2P2 = new ConnectPoint(DID2, P2);
        final LinkKey linkId1 = new LinkKey(d1P1, d2P2);

        final CountDownLatch addLatch = new CountDownLatch(1);
        LinkStoreDelegate checkAdd = new LinkStoreDelegate() {
            @Override
            public void notify(LinkEvent event) {
                assertEquals(LINK_ADDED, event.type());
                assertLink(linkId1, INDIRECT, event.subject());
                addLatch.countDown();
            }
        };
        final CountDownLatch updateLatch = new CountDownLatch(1);
        LinkStoreDelegate checkUpdate = new LinkStoreDelegate() {
            @Override
            public void notify(LinkEvent event) {
                assertEquals(LINK_UPDATED, event.type());
                assertLink(linkId1, DIRECT, event.subject());
                updateLatch.countDown();
            }
        };
        final CountDownLatch removeLatch = new CountDownLatch(1);
        LinkStoreDelegate checkRemove = new LinkStoreDelegate() {
            @Override
            public void notify(LinkEvent event) {
                assertEquals(LINK_REMOVED, event.type());
                assertLink(linkId1, DIRECT, event.subject());
                removeLatch.countDown();
            }
        };

        linkStore.setDelegate(checkAdd);
        putLink(linkId1, INDIRECT);
        assertTrue("Add event fired", addLatch.await(1, TimeUnit.SECONDS));

        linkStore.unsetDelegate(checkAdd);
        linkStore.setDelegate(checkUpdate);
        putLink(linkId1, DIRECT);
        assertTrue("Update event fired", updateLatch.await(1, TimeUnit.SECONDS));

        linkStore.unsetDelegate(checkUpdate);
        linkStore.setDelegate(checkRemove);
        linkStore.removeLink(d1P1, d2P2);
        assertTrue("Remove event fired", removeLatch.await(1, TimeUnit.SECONDS));
    }
}