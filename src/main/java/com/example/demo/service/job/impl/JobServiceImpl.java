package com.example.demo.service.job.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.example.demo.base.response.BaseResponse;
import com.example.demo.base.response.NotFoundResponse;
import com.example.demo.dao.FarmerJobRepository;
import com.example.demo.dao.FarmerRepository;
import com.example.demo.dao.JobRepository;
import com.example.demo.dao.UserRepository;
import com.example.demo.dto.job.CreatedJobDTO;
import com.example.demo.dto.job.FarmerJobDTO;
import com.example.demo.dto.job.JobDTO;
import com.example.demo.entity.CustomUserDetails;
import com.example.demo.entity.Workplace;
import com.example.demo.entity.job.Job;
import com.example.demo.entity.job.JobStatus;
import com.example.demo.entity.user.Farmer;
import com.example.demo.entity.user.FarmerJob;
import com.example.demo.entity.user.FarmerJobID;
import com.example.demo.entity.user.FarmerJobStatus;
import com.example.demo.entity.user.User;
import com.example.demo.mapping.job.JobMapping;
import com.example.demo.service.job.JobService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JobServiceImpl implements JobService {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private FarmerJobRepository fJobRepository;

    @Autowired
    private JobMapping jobMapping;

    @Autowired
    private UserRepository userRepository;

    @Override
    public BaseResponse getAllJobs() {
        List<Job> jobs = jobRepository.findAll();
        List<JobDTO> jobDTOs = new ArrayList<>();
        for (Job job : jobs) {
            JobDTO jobDTO = new JobDTO();
            jobDTO.setName(job.getName());
            jobDTO.setId(job.getId());
            jobDTO.setImageUrl(job.getImageUrl());
            jobDTO.setAddress(job.getAddress());
            jobDTO.setCreatedAt(job.getCreatedAt());
            jobDTO.setContact(job.getContact());
            jobDTO.setContactNumber(job.getContactNumber());
            jobDTO.setJobDetail(job.getJobDetail());
            jobDTO.setSalary(job.getSalary());
            jobDTO.setDescription(job.getDescription());
            jobDTO.setDue(job.getDue());
            jobDTO.setArea(job.getWorkplace().getArea());
            jobDTO.setJobStatus(job.getJobStatus().toString());
            jobDTOs.add(jobDTO);

        }
        return new BaseResponse<>(HttpStatus.OK, "All jobs", jobDTOs);
        // jobs.stream().map(job ->
        // jobMapping.mapJobtoJobDTO(job)).collect(Collectors.toList()));
    }

    @Override
    public BaseResponse getJob(int jobId) {
        try {
            Job job = jobRepository.getById(jobId);
            JobDTO jobDTO = jobMapping.mapJobtoJobDTO(job);
            return new BaseResponse<JobDTO>(HttpStatus.OK, "All jobs", jobDTO);
        } catch (Exception e) {
            return new NotFoundResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }

    }

    @Override
    public BaseResponse createJob(JobDTO jobDTO) {
        try {

            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal();

            User user = userRepository.findById(userDetails.getUser().getId()).get();

            Job job = jobMapping.mapJobDtoToJob(jobDTO);
            job.setOwner(user);

            Workplace workplace = new Workplace();
            workplace.setAddress(jobDTO.getAddress());
            workplace.setArea(jobDTO.getArea());
            job.setCreatedAt(new Date());
            job.setWorkplace(workplace);

            jobRepository.save(job);

            return new BaseResponse<JobDTO>(HttpStatus.OK, "Add successfully!", jobMapping.mapJobtoJobDTO(job));
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            return new NotFoundResponse(HttpStatus.BAD_REQUEST, "Add failed! " + e.toString());
        }

    }

    @Override
    public BaseResponse removeJob(int jobId) {
        try {
            jobRepository.deleteById(jobId);
            return new BaseResponse<JobDTO>(HttpStatus.OK, "Remove successfully!");
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            return new NotFoundResponse(HttpStatus.BAD_REQUEST, "Remove failed! " + e.getMessage());
        }

    }

    @Override
    public BaseResponse assignJob(FarmerJobDTO farmerJobDTO, int jobId) {

        try {
            FarmerJobID fjId = new FarmerJobID(jobId, farmerJobDTO.getWorkerId());
            Optional<FarmerJob> farmerJobOpt = fJobRepository.findById(fjId);
            FarmerJob farmerJob = farmerJobOpt.get();
            farmerJob.getJob().setJobStatus(JobStatus.PENDING);
            farmerJob.setAcceptedAt(new Date());
            farmerJob.setStatus(FarmerJobStatus.ACCEPTED);

            fJobRepository.save(farmerJob);

            farmerJobDTO.setAcceptedAt(farmerJob.getAcceptedAt());
            farmerJobDTO.setStatus(farmerJob.getStatus().toString());

            return new BaseResponse<>(HttpStatus.OK, "Assign job successful!", farmerJobDTO);
        } catch (Exception ex) {

            log.error(ex.getMessage(), ex.getCause());
            return new BaseResponse<>(HttpStatus.BAD_REQUEST, "Assign failed! " + ex.getMessage());

        }

    }

    @Override
    public BaseResponse receiveJob(FarmerJobDTO farmerJobDTO, int jobId) {

        try {

            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal();
            User user = userRepository.findById(userDetails.getUser().getId()).get();

            FarmerJobID fjId = new FarmerJobID(jobId, user.getId());
            FarmerJob farmerJob = new FarmerJob();
            farmerJob.setFarmerJobID(fjId);
            farmerJob.setReceivedAt(new Date());
            farmerJob.setComment(farmerJobDTO.getComment());
            farmerJob.setDealPrice(farmerJobDTO.getDealPrice());
            farmerJob.setStatus(FarmerJobStatus.REQUESTING);

            fJobRepository.save(farmerJob);

            farmerJobDTO.setJobId(jobId);
            farmerJobDTO.setReceivedAt(farmerJob.getReceivedAt());
            farmerJobDTO.setStatus(farmerJob.getStatus().toString());

            return new BaseResponse<>(HttpStatus.OK, "Receive job successful!", farmerJobDTO);

        } catch (Exception ex) {
            return new BaseResponse<>(HttpStatus.BAD_REQUEST, "Receive failed! " + ex.getMessage());

        }

    }

    @Override
    public BaseResponse completedJob(int jobId) {

        try {
            Job job = jobRepository.getById(jobId);

            job.setJobStatus(JobStatus.COMPLETED);

            jobRepository.save(job);

            JobDTO jobDTO = jobMapping.mapJobtoJobDTO(job);
            return new BaseResponse<>(HttpStatus.OK, "Job completed!", jobDTO);
        } catch (Exception e) {

            return new BaseResponse<>(HttpStatus.BAD_REQUEST, "Complete job failed! " + e.getMessage());
        }
    }

    @Override
    public BaseResponse rejectJob(FarmerJobDTO farmerJobDTO, int jobId) {

        try {
            FarmerJobID fjId = new FarmerJobID(jobId, farmerJobDTO.getWorkerId());
            Optional<FarmerJob> farmerJobOpt = fJobRepository.findById(fjId);

            FarmerJob farmerJob = farmerJobOpt.get();
            farmerJob.setStatus(FarmerJobStatus.REJECTED);

            fJobRepository.save(farmerJob);

            farmerJobDTO.setStatus(farmerJob.getStatus().toString());

            return new BaseResponse<>(HttpStatus.OK, "Reject successfull", farmerJobDTO);

        } catch (Exception e) {

            log.error(e.getMessage(), e.getCause());
            return new BaseResponse<>(HttpStatus.BAD_GATEWAY, "Reject failed! " + e.getMessage());

        }

    }

    @Override
    public BaseResponse getCreatedJobs() {

        try {
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal();
            User user = userRepository.findById(userDetails.getUser().getId()).get();

            Collection<Job> jobs = user.getCreatedJobs();

            List<JobDTO> jobDTOs = new ArrayList<>();
            for (Job job : jobs) {
                JobDTO jobDTO = new JobDTO();
                jobDTO.setName(job.getName());
                jobDTO.setId(job.getId());
                jobDTO.setImageUrl(job.getImageUrl());
                jobDTO.setAddress(job.getAddress());
                jobDTO.setCreatedAt(job.getCreatedAt());
                jobDTO.setContact(job.getContact());
                jobDTO.setContactNumber(job.getContactNumber());
                jobDTO.setJobDetail(job.getJobDetail());
                jobDTO.setSalary(job.getSalary());
                jobDTO.setDescription(job.getDescription());
                jobDTO.setDue(job.getDue());
                jobDTO.setArea(job.getWorkplace().getArea());
                jobDTO.setJobStatus(job.getJobStatus().toString());
                jobDTOs.add(jobDTO);

            }
            return new BaseResponse<>(HttpStatus.OK, "All created jobs", jobDTOs);

        } catch (Exception e) {

            log.error(e.getMessage(), e.getCause());
            return new BaseResponse<>(HttpStatus.OK, "Get job error");

        }

    }

    @Override
    public BaseResponse getReceivedJobs() {

        try {
            CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal();
            User user = userRepository.findById(userDetails.getUser().getId()).get();

            Collection<FarmerJob> farmerJobs = user.getDoJobs();
            List<JobDTO> jobDTOs = new ArrayList<>();
            for (FarmerJob farmerJob : farmerJobs) {
                Job job = farmerJob.getJob();
                JobDTO jobDTO = new JobDTO();
                jobDTO.setName(job.getName());
                jobDTO.setId(job.getId());
                jobDTO.setImageUrl(job.getImageUrl());
                jobDTO.setAddress(job.getAddress());
                jobDTO.setCreatedAt(job.getCreatedAt());
                jobDTO.setContact(job.getContact());
                jobDTO.setContactNumber(job.getContactNumber());
                jobDTO.setJobDetail(job.getJobDetail());
                jobDTO.setSalary(job.getSalary());
                jobDTO.setDescription(job.getDescription());
                jobDTO.setDue(job.getDue());
                jobDTO.setArea(job.getWorkplace().getArea());
                jobDTO.setJobStatus(job.getJobStatus().toString());
                jobDTOs.add(jobDTO);

            }
            return new BaseResponse<>(HttpStatus.OK, "All created jobs", jobDTOs);
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            return new BaseResponse<>(HttpStatus.OK, "Get job error");
        }

    }

    @Override
    public BaseResponse getCreatedJobDetail(int jobId) {

        try {
            CreatedJobDTO createdJobDTO = new CreatedJobDTO();

            Job job = jobRepository.findById(jobId).get();

            JobDTO jobDTO = jobMapping.mapJobtoJobDTO(job);
            createdJobDTO.setJob(jobDTO);

            Collection<FarmerJob> farmerJobs = job.getContacts();

            for (FarmerJob farmerJob : farmerJobs) {
                Farmer farmer = farmerJob.getWorker();

                FarmerJobDTO farmerJobDTO = new FarmerJobDTO();
                farmerJobDTO.setComment(farmerJob.getComment());
                farmerJobDTO.setReceivedAt(farmerJob.getReceivedAt());
                farmerJobDTO.setDealPrice(farmerJob.getDealPrice());
                farmerJobDTO.setUsername(farmer.getName());
                farmerJobDTO.setPhone(farmer.getPhone());
                farmerJobDTO.setStatus(farmerJob.getStatus().toString());
                farmerJobDTO.setReceivedAt(farmerJob.getReceivedAt());

                createdJobDTO.getReceivers().add(farmerJobDTO);

            }
            return new BaseResponse<>(HttpStatus.OK, "All created jobs", createdJobDTO);
        } catch (Exception e) {
            log.error(e.getMessage(), e.getCause());
            return new BaseResponse<>(HttpStatus.OK, "Get job error");
        }
    }
}
