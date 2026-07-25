package com.smartmatcher.service;

import com.smartmatcher.model.JobOffer;
import java.util.List;

public interface JobSearchProvider {
    List<JobOffer> searchJobs(String keywords, String location, String contractType);
}
