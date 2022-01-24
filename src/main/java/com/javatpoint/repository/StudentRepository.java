package com.javatpoint.repository;

import com.javatpoint.model.Student;
import org.springframework.data.repository.CrudRepository;
public interface StudentRepository extends CrudRepository<Student, Integer> {
}
