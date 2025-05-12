package com.cyberlog;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    private int id;
    private String name;
    private int marks;
}
