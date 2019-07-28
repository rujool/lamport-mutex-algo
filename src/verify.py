#!/usr/bin/env python
with open('../config.txt','r') as config_file:
    config_line = map(int,config_file.readline().strip().split(" "))
N = config_line[0]
num_req = config_line[3]
count = 0
prev,prev_hostname = ('','')
map_req_count = {}
failed = False
with open('../logs.txt','r') as log_file:
    for line in log_file:
        hostname = line.split(" ")[0]
        count += 1
        if (prev == '' or prev =='leave') and ('leaving' in line or 'entering' not in line):
            failed = True
            break
        elif prev == 'enter' and ('entering' in line or 'leaving' not in line):
            failed = True
            break
        elif prev not in ['','enter','leave']:
            failed = True
            break
        if 'leaving' in line:
            prev = 'leave'
            if prev_hostname != hostname:
                failed = True
                break
            map_req_count[hostname] += 0.5
        elif 'entering' in line:
            prev = 'enter'
            prev_hostname = hostname
            if hostname not in map_req_count:
                map_req_count[hostname] = 0.5
            else:
                map_req_count[hostname] += 0.5
        else:
            prev = 'none'
    if count != 2 * N * num_req:
        print(count)
        failed = True
        print("Number of CS requests satisfied is not as expected")
for hostname in map_req_count:
    if map_req_count[hostname] != float(num_req):
       failed = True
if not failed:
    print("Mutex satisfied, protocol correctly implemented")
else:
    print("Mutex not satisfied")