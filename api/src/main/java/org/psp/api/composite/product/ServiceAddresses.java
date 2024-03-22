package org.psp.api.composite.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAddresses {
    private String cmp;
    private String pro;
    private String rev;
    private String rec;
}
