def main():
    list = []
    flag = 0
    with open('/etc/logstash/indexer.conf') as fp:
        for line in fp:
            if line.__contains__("ganglia"):
                flag = 1
            elif flag > 0:
                if line.__contains__("}"):
                    flag = 0
                continue

            else:
                list.append(line)

    write_to = open('/etc/logstash/indexer.conf', 'w')
    for item in list:
        write_to.write("%s" % item)
if "__main__" == __name__:
    main()
