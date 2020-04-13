package it.cnr.iit.ck.model;

import java.util.List;

public interface MultiLoggable {
    List<String> getRowsToLog();
    boolean isEmpty();
}
