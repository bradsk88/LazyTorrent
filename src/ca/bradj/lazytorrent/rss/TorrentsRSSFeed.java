package ca.bradj.lazytorrent.rss;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import ca.bradj.common.base.Failable;
import ca.bradj.lazytorrent.app.AlreadyDownloaded;
import ca.bradj.lazytorrent.app.Logger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class TorrentsRSSFeed implements RSSFeed {

    private final List<RSSUpdateListener> listeners = Lists.newArrayList();

    private URL url;

    private final AlreadyDownloaded already;

    private final Logger logger;

    private static final String ITEM = "item";

    private static final String CHAN = "channel";

    private static final Extension NAME_EXT = new Extension() {
        @Override
        public Void apply(DefaultRSSTorrent.Builder b, String extension) {
            b.nameExtend(extension);
            return null;
        }
    };

    private static final Extension LINK_EXT = new Extension() {
        @Override
        public Void apply(DefaultRSSTorrent.Builder b, String extension) {
            b.linkExtend(extension);
            return null;
        }
    };

    private static final Extension DESC_EXT = new Extension() {
        @Override
        public Void apply(DefaultRSSTorrent.Builder b, String extension) {
            b.descriptionExtend(extension);
            return null;
        }
    };

    private static final Failable<RSSTorrent> EXPECTED_START_TAG = Failable.fail("Expected start tag");

    public TorrentsRSSFeed(String torrentsURL, AlreadyDownloaded alreadyDownloaded, Logger logger) {
        this.already = alreadyDownloaded;
        this.logger = logger;
        try {
            this.url = new URL(torrentsURL);
        } catch (MalformedURLException e) {
            logger.error("Exception: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<RSSTorrent> requestRefresh() {
        Collection<RSSTorrent> t = getNewTorrents();
        t = strip(t, already);
        logger.debug(t.size() + " torrents read from server");
        updateListeners(t);
        return t;
    }

    protected ObservableList<RSSTorrent> strip(Collection<RSSTorrent> torrents, AlreadyDownloaded alreadyDownloaded2) {
        ObservableList<RSSTorrent> list = FXCollections.observableArrayList();
        for (RSSTorrent i : torrents) {
            if (alreadyDownloaded2.hasSameNameAndEpisode(i)) {
                continue;
            }
            list.add(i);
        }
        return list;
    }

    private void updateListeners(Collection<RSSTorrent> t) {
        Preconditions.checkNotNull(t);

        ImmutableList<RSSTorrent> copyof = ImmutableList.copyOf(t);
        for (RSSUpdateListener l : listeners) {
            l.newTorrentsListAvailable(copyof);
        }
    }

    private Collection<RSSTorrent> getNewTorrents() {

        Collection<RSSTorrent> torrents = Lists.newArrayList();
        // First create a new XMLInputFactory
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        try (InputStream in = url.openStream()) {

            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

            while (eventReader.hasNext()) {
                // TODO Something with the header?
                XMLEvent nextEvent = eventReader.nextEvent();
                if (nextEvent.isStartElement()) {
                    String lp = nextEvent.asStartElement().getName().getLocalPart();
                    if (is(CHAN, nextEvent)) {
                        break;
                    }
                    System.out.println(lp);
                }
            }

            // read the XML document
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    if (is(ITEM, event)) {
                        Failable<RSSTorrent> makeTorrent = makeTorrent(eventReader);
                        if (makeTorrent.isFailure()) {
                            logger.debug("Problem parsing feed: " + makeTorrent.getReason());
                            continue;
                        }
                        torrents.add(makeTorrent.get());
                        continue;
                    }
                    if (is("description", event)) {
                        continue;
                    }
                }
                if (event.isEndElement()) {
                    if (is(CHAN, event)) {
                        break;
                    }
                }
                throw new IllegalStateException("Torrent item not fully parsed.  Event was " + event);
            }
        } catch (XMLStreamException | IOException e) {
            logger.error("Problem reading RSS feed: " + e.getMessage());
            return Collections.emptyList();
        }
        return torrents;
    }

    private Failable<RSSTorrent> makeTorrent(XMLEventReader eventReader) throws XMLStreamException {
        DefaultRSSTorrent.Builder b = DefaultRSSTorrent.builder();

        if (!preRead(eventReader)) {
            return EXPECTED_START_TAG;
        }
        XMLEvent nextEvent = eventReader.nextEvent();
        b.name(nextEvent.asCharacters().getData());
        readLong(b, eventReader, NAME_EXT);
        if (!preRead(eventReader)) {
            return EXPECTED_START_TAG;
        }
        b.link(eventReader.nextEvent().asCharacters().getData());
        readLong(b, eventReader, LINK_EXT);
        if (!preRead(eventReader)) {
            return EXPECTED_START_TAG;
        }
        b.date(eventReader.nextEvent().asCharacters().getData());
        postRead(eventReader);
        if (!preRead(eventReader)) {
            return EXPECTED_START_TAG;
        }
        b.description(eventReader.nextEvent().asCharacters().getData());
        readLong(b, eventReader, DESC_EXT);
        postRead(eventReader);
        return Failable.ofSuccess((RSSTorrent) b.build());
    }

    private void readLong(DefaultRSSTorrent.Builder b, XMLEventReader eventReader, Extension linkExt)
            throws XMLStreamException {
        while (true) {
            XMLEvent nextEvent2 = eventReader.nextEvent();
            if (nextEvent2.isEndElement()) {
                break;
            }
            String data = nextEvent2.asCharacters().getData();
            linkExt.apply(b, data);
        }
    }

    private boolean preRead(XMLEventReader eventReader) throws XMLStreamException {
        XMLEvent nextEvent = eventReader.nextEvent();
        if (nextEvent.isStartElement()) {
            return true;
        }
        return false;
        // throw new FormatChangedException("Expected start tag");
    }

    private void postRead(XMLEventReader eventReader) throws XMLStreamException {
        XMLEvent nextEvent = eventReader.nextEvent();
        if (nextEvent.isEndElement()) {
            return;
        }
		// throw new FormatChangedException("Expected end tag but was " +
        // nextEvent);
    }

    private boolean is(String constant, XMLEvent event) {
        Preconditions.checkNotNull(constant);
        if (event.isStartElement()) {
            StartElement asStartElement = event.asStartElement();
            return doIs(constant, asStartElement.getName());
        }
        if (event.isEndElement()) {
            EndElement asEnd = event.asEndElement();
            return doIs(constant, asEnd.getName());
        }
        throw new RuntimeException("Unexpected tag " + event);
    }

    private boolean doIs(String constant, QName name) {
        String localPart = name.getLocalPart();
        return constant.equals(localPart);
    }

    @Override
    public void addUpdateListener(RSSUpdateListener listener) {
        Preconditions.checkNotNull(listener);
        this.listeners.add(listener);
    }

}
