package com.op1m.medrem.backend_api.dto;

import java.util.List;

public class BulkDeleteResponse {
    private int deletedCount;
    private int notFoundCount;
    private List<Long> notFoundIds;

    public BulkDeleteResponse() {}

    public BulkDeleteResponse(int deletedCount, int notFoundCount, List<Long> notFoundIds) {
        this.deletedCount = deletedCount;
        this.notFoundCount = notFoundCount;
        this.notFoundIds = notFoundIds;
    }

    public int getDeletedCount() { return deletedCount; }
    public void setDeletedCount(int deletedCount) { this.deletedCount = deletedCount; }

    public int getNotFoundCount() { return notFoundCount; }
    public void setNotFoundCount(int notFoundCount) { this.notFoundCount = notFoundCount; }

    public List<Long> getNotFoundIds() { return notFoundIds; }
    public void setNotFoundIds(List<Long> notFoundIds) { this.notFoundIds = notFoundIds; }
}