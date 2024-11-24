package com.sithumya20220865.OOPCW;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RepositoryService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final VendorRepository vendorRepository;
    private final AdminRepository adminRepository;
    private final TicketRepository ticketRepository;

    @Autowired
    public RepositoryService(UserRepository userRepository, CustomerRepository customerRepository,
                             VendorRepository vendorRepository, AdminRepository adminRepository,
                             TicketRepository ticketRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.vendorRepository = vendorRepository;
        this.adminRepository = adminRepository;
        this.ticketRepository = ticketRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public CustomerRepository getCustomerRepository() {
        return customerRepository;
    }

    public VendorRepository getVendorRepository() {
        return vendorRepository;
    }

    public AdminRepository getAdminRepository() {
        return adminRepository;
    }

    public TicketRepository getTicketRepository() {
        return ticketRepository;
    }
}
