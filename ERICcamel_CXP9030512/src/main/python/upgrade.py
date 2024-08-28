import optparse
import sys
import pycurl
import cStringIO

def printOpts(options):
	for k,v in options.__dict__.iteritems():
		print k,': ', v

def validate(options):
	invalid=0
	for k,v in options.__dict__.iteritems():
		if v == None or v == '':
			print k, 'is Null'
			invalid+=1

	
	if invalid>0:
		print 'There were errors. Not proceeding.'
		sys.exit(1)

def readArguments():
	"""
	See http://docs.python.org/library/optparse.html#optparse.Option.type for documentation on optparse
	It's a deprecated library, but the recommended replacement is only in python 2.7, we use 2.6.
	Notes:
	Type can be: "string", "int", "long", "choice", "float" and "complex" -> designates the type of the cli arg
	"""
	parser = optparse.OptionParser()
	parser.add_option("-a", "--app_server_address", action="store", type="string",
		help="Specifies the IPAddress of the host that the app_server instance is running on, and the port on which the upgrade REST interface is exposed.", metavar="ADDRESS")
	parser.add_option("-u", "--user", action="store", type="string",
		help="User to authenticate the REST call and create connections towards JMS. This user must be configured for each app_server instance", metavar="USER")
	parser.add_option("-k", "--password", action="store", type="string",
		help="The password corresponding with USER", metavar="PASSWORD")
	parser.add_option("-d", "--app_server_identifier", action="store", type="string",
		help="The application server identity", metavar="SERVER_ID")
	parser.add_option("-i", "--service_identifier", action="store", type="string", 
		help="The identity of the service being upgraded", metavar="SERVICE_ID")
	parser.add_option("-o", "--upgrade_operation_type", action="store", type="string", 
		help="The Upgrade type", metavar="SERVICE")
	parser.add_option("-s", "--upgrade_phase", action="store", type="string",
		help="The phase of the upgrade operation type to be executed", metavar="PREPARE")
		
	return parser.parse_args()

def start(o):
	timeoutVar=10
	
	buf = cStringIO.StringIO()
	c=pycurl.Curl()
	c.setopt(c.URL, o.app_server_address)
	c.setopt(c.WRITEFUNCTION, buf.write)
	c.setopt(c.TIMEOUT, timeoutVar)
	c.setopt(c.FAILONERROR, True)
	try:
		c.perform()
	except pycurl.error, error:
		errno, errstr = error
		print 'An error occurred with curl during the connection: ', errstr
		sys.exit(2)
	
	print "..."
	print buf.getvalue()
	buf.close()

(opts,args) = readArguments()

validate(opts)
printOpts(opts)




if 'notifyUpgrade' in args:
	print "Upgrade notification sent."
	start(opts)
	print "Upgrade complete."



	
