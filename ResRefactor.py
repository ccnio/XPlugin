import codecs
import os
import os.path
import shutil
import sys
import xml.etree.ElementTree as eTree


def move_file(src_path, dst_path, file):
    try:
        f_src = os.path.join(src_path, file)
        if not os.path.exists(dst_path):
            os.mkdir(dst_path)
        f_dst = os.path.join(dst_path, file)
        shutil.move(f_src, f_dst)
    except Exception as e:
        print(e)
        exit(0)


def file_name(file):
    return os.path.basename(file).split('.')[0]


def read_r_file(path):
    file = open(path, 'r')
    types = dict()  # res type -> res set
    lines = file.readlines()
    for lines in lines:
        res_type = lines.split(' ')[1]
        res_name = lines.split(' ')[2]
        if res_type in types:
            types[res_type].add(res_name)
        else:
            res_set = set()
            res_set.add(res_name)
            types[res_type] = res_set
    return types


def traverse_used(res_type, filepath, res_name):
    path_dir = os.listdir(filepath)
    for dirs in path_dir:
        child = os.path.join('%s/%s' % (filepath, dirs))
        if os.path.isfile(child):
            # print("res name: %s, file = %s" % (res_name, child))
            if not _is_binary_file(child):
                # print("file: %s" % child)
                if filter_file_str(res_type, child, res_name):
                    return True
            continue
        else:
            # print('not file')
            if traverse_used(res_type, child, res_name):
                return True
    return False


def exist_in_xml(res_type, filepath, res_name):
    path_dir = os.listdir(filepath)
    for dirs in path_dir:
        child = os.path.join('%s/%s' % (filepath, dirs))
        if not os.path.isfile(child) or not child.endswith('.xml'):
            continue
        tree = eTree.parse(child)
        findall = tree.findall(".//%s[@name=\"%s\"]" % (res_type, res_name))
        if len(findall) > 0:
            # print("find %s::%s in %s" % (res_type, res_name, child))
            return True, child
    return False, ""


def filter_file_str(res_type, filename, res_name):
    fop = open(filename, 'r')
    content = fop.read()
    fop.close()
    des_str1 = 'R.' + res_type + '.' + res_name
    des_str2 = '@' + res_type + '/' + res_name
    # print("desStr1: %s; desStr2: %s" % (des_str1, des_str2))
    if des_str1 in content or des_str2 in content:
        # print('%s::%s, used in %s' % (res_type, res_name, filename))
        return True
    else:
        return False


_TEXT_BOM = (
    codecs.BOM_UTF16_BE,
    codecs.BOM_UTF16_LE,
    codecs.BOM_UTF32_BE,
    codecs.BOM_UTF32_LE,
    codecs.BOM_UTF8,
)


def _is_binary_file(file_path):
    with open(file_path, 'rb') as file:
        initial_bytes = file.read(8192)
        file.close()
        for bom in _TEXT_BOM:
            if initial_bytes.startswith(bom):
                continue
            else:
                if b'\0' in initial_bytes:
                    return True
    return False


def file_in_dir(res_name, res_type, filepath):
    if not os.path.exists(filepath):
        return False, ''
    path_dir = os.listdir(filepath)
    for dirs in path_dir:
        child = os.path.join('%s/%s' % (filepath, dirs))
        if os.path.isfile(child):
            if file_name(child) == res_name:
                # print("find %s::%s in %s" % (res_type, res_name, child))
                return True, child
        else:
            ret, path = file_in_dir(res_name, res_type, child)
            if ret:
                # print("find %s::%s in %s" % (res_type, res_name, path))
                return True, path
    return False, ""


def used_in_project(res_type, res_name, pro_dir):
    return traverse_used(res_type, pro_dir, res_name)


def used_in_projects(res_type, res_name, pro_dirs):
    for pro_dir in pro_dirs:
        if traverse_used(res_type, pro_dir, res_name):
            return True
    return False


def main(src_dir, des_dir, des_r_file, exist_dirs):
    if not os.path.exists(des_r_file):
        print("R.txt not exists, should build pro before execute this py")
        exit(0)

    print("ResRefactor: src=%s, des=%s, r=%s, exist_dirs=%s\n" % (src_dir, des_dir, des_r_file, exist_dirs))

    resource_map = read_r_file(des_r_file)  # key->set
    build_dir = os.path.abspath('.') + "/build/"
    if not os.path.exists(build_dir):
        os.makedirs(build_dir)
    output_path = build_dir + "res_need_refactor.log"
    file = open(output_path, 'w')

    for res_type in resource_map.keys():
        for resource in resource_map[res_type]:
            # if resource != 'activity_common':
            #     continue
            des_used = used_in_project(res_type, resource, des_dir)
            # print("des_used: %s" % des_used)
            if not des_used:
                continue

            des_exist = format_in_project(res_type, resource, des_dir)[0]
            # print("des_exist: %s @%s" % (des_exist, resource))
            if des_exist:
                continue

            other_used = used_in_projects(res_type, resource, exist_dirs)
            # print("other used: %s" % other_used)
            if other_used:
                continue

            in_src, type_, path = format_in_project(res_type, resource, src_dir)
            if not in_src:
                continue

            ret = "%s::%s::%s" % (type_, resource, path)
            file.write(ret + "\n")
            print(ret)
    file.close()
    print("\nsee detail in %s" % output_path)


def format_in_project(res_type, resource, des_dir):
    _in = False
    path = ""
    _type = ""
    if res_type == 'layout' or res_type == 'anim' or res_type == 'animator' or res_type == 'drawable' \
            or res_type == 'xml' or res_type == 'interpolator' or res_type == 'font':
        _in, path = file_in_dir(resource, res_type, des_dir + '/res')
        _type = res_type
    elif res_type == 'bool' or res_type == '' or res_type == 'dimen' or res_type == 'integer' \
            or res_type == 'plurals' or res_type == 'string' \
            or res_type == 'style' or res_type == 'styleable':
        if res_type == 'styleable':
            res_type = 'declare-styleable'
        _in, path = exist_in_xml(res_type, des_dir + '/res/values', resource)
        _type = res_type
    elif res_type == 'array':
        _in, path = exist_in_xml('string-array', des_dir + '/res/values', resource)
        _type = 'string-array'
        if not _in:
            _in, path = exist_in_xml('integer-array', des_dir + '/res/values', resource)
            _type = 'integer-array'
        if not _in:
            _in, path = exist_in_xml('array', des_dir + '/res/values', resource)
            _type = 'array'
    elif res_type == 'color':
        _in, path = file_in_dir(resource, res_type, des_dir + '/res/color')
        _type = 'color@file'
        if not _in:
            _in, path = exist_in_xml(res_type, des_dir + '/res/values', resource)
            _type = 'color@value'
    else:
        print("ignored type: %s" % res_type)
    return _in, _type, path


is_debug = True
if __name__ == '__main__':
    main(src_dir='/home/lijf/code/XPlugin/common-library/src/main' if is_debug else sys.argv[1],
         des_dir='/home/lijf/code/XPlugin/mylibrary/src/main' if is_debug else sys.argv[2],
         des_r_file='/home/lijf/PycharmProjects/logProject/R.txt' if is_debug else sys.argv[3],
         exist_dirs=['/home/lijf/code/XPlugin/library2/src/main',
                     '/home/lijf/code/XPlugin/library3/src/main'] if is_debug else sys.argv[4:len(sys.argv)])
